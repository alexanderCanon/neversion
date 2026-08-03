use std::collections::HashSet;
use tonic::{Request, Response, Status};
use uuid::Uuid;

use crate::db::DbPool;
use crate::email;

pub mod proto {
    tonic::include_proto!("neversion.notification");
}

use proto::notification_service_server::NotificationService;
use proto::{CheckExistsRequest, CheckExistsResponse, NotificationRequest, NotificationResponse};

pub struct MyNotificationService {
    pub db: DbPool,
    pub http_client: reqwest::Client,
    pub resend_api_key: String,
    pub from_email: String,
    pub internal_types: HashSet<&'static str>,
}

impl MyNotificationService {
    pub fn new(db: DbPool, resend_api_key: String, from_email: String) -> Self {
        let mut internal_types = HashSet::new();
        internal_types.insert("NO_INVENTORY_ALERT");
        internal_types.insert("SUBSCRIPTIONS_EXPIRED_DAILY");

        Self {
            db,
            http_client: reqwest::Client::new(),
            resend_api_key,
            from_email,
            internal_types,
        }
    }
}

#[tonic::async_trait]
impl NotificationService for MyNotificationService {
    async fn send_notification(
        &self,
        request: Request<NotificationRequest>,
    ) -> Result<Response<NotificationResponse>, Status> {
        let req = request.into_inner();
        let notification_uuid = Uuid::new_v4().to_string();

        println!(
            "Received SendNotification request: event_type={}, recipient={}, reference_type={}, reference_id={}, tags={}",
            req.event_type, req.recipient, req.reference_type, req.reference_id, req.tags
        );

        // 1. Insert log as 'pending'
        let insert_res = sqlx::query(
            r#"
            INSERT INTO notification_logs 
            (uuid, event_type, recipient, payload, reference_type, reference_id, tags, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', datetime('now'))
            "#,
        )
        .bind(&notification_uuid)
        .bind(&req.event_type)
        .bind(&req.recipient)
        .bind(if req.payload.is_empty() { None } else { Some(&req.payload) })
        .bind(if req.reference_type.is_empty() { None } else { Some(&req.reference_type) })
        .bind(if req.reference_id == 0 { None } else { Some(req.reference_id) })
        .bind(if req.tags.is_empty() { None } else { Some(&req.tags) })
        .execute(&self.db)
        .await;

        if let Err(e) = insert_res {
            eprintln!("Failed to write pending log to SQLite: {}", e);
            return Ok(Response::new(NotificationResponse {
                success: false,
                message: format!("Failed to initialize log: {}", e),
                notification_uuid: String::new(),
            }));
        }

        // Check if internal-only event (skip sending email)
        if self.internal_types.contains(req.event_type.as_str()) {
            println!("Event {} is internal-only. Skipping email dispatch.", req.event_type);
            let _ = sqlx::query(
                "UPDATE notification_logs SET status = 'success', processed_at = datetime('now') WHERE uuid = ?",
            )
            .bind(&notification_uuid)
            .execute(&self.db)
            .await;

            return Ok(Response::new(NotificationResponse {
                success: true,
                message: "Internal notification recorded (email skipped)".to_string(),
                notification_uuid,
            }));
        }

        // 2. Dispatch email via Resend
        let dispatch_result = email::dispatch_email(
            &self.http_client,
            &self.resend_api_key,
            &self.from_email,
            &req.recipient,
            &req.event_type,
            &req.payload,
        )
        .await;

        match dispatch_result {
            Ok(msg) => {
                let _ = sqlx::query(
                    "UPDATE notification_logs SET status = 'success', processed_at = datetime('now') WHERE uuid = ?",
                )
                .bind(&notification_uuid)
                .execute(&self.db)
                .await;

                Ok(Response::new(NotificationResponse {
                    success: true,
                    message: msg,
                    notification_uuid,
                }))
            }
            Err(err_msg) => {
                let truncated_err = if err_msg.len() > 500 {
                    &err_msg[..500]
                } else {
                    &err_msg
                };

                let _ = sqlx::query(
                    "UPDATE notification_logs SET status = 'failed', error_message = ?, processed_at = datetime('now') WHERE uuid = ?",
                )
                .bind(truncated_err)
                .bind(&notification_uuid)
                .execute(&self.db)
                .await;

                Ok(Response::new(NotificationResponse {
                    success: false,
                    message: err_msg,
                    notification_uuid,
                }))
            }
        }
    }

    async fn check_exists(
        &self,
        request: Request<CheckExistsRequest>,
    ) -> Result<Response<CheckExistsResponse>, Status> {
        let req = request.into_inner();

        println!(
            "Received CheckExists request: reference_type={}, reference_id={}, tags={}",
            req.reference_type, req.reference_id, req.tags
        );

        let row: Option<(i64,)> = sqlx::query_as(
            r#"
            SELECT 1 FROM notification_logs 
            WHERE reference_type = ? AND reference_id = ? AND tags = ? AND status = 'success'
            LIMIT 1
            "#,
        )
        .bind(&req.reference_type)
        .bind(req.reference_id)
        .bind(&req.tags)
        .fetch_optional(&self.db)
        .await
        .map_err(|e| Status::internal(format!("CheckExists query failed: {}", e)))?;

        let exists = row.is_some();
        println!("CheckExists result: {}", exists);

        Ok(Response::new(CheckExistsResponse { exists }))
    }
}
