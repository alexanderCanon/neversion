use tonic::{Request, Response, Status};
use uuid::Uuid;

use crate::db::DbPool;

pub mod proto {
    tonic::include_proto!("neversion.reservation");
}

use proto::reservation_service_server::ReservationService;
use proto::{
    CheckActiveRenewalRequest, CheckActiveRenewalResponse, CreateReservationRequest,
    ExpirePendingReservationsRequest, ExpirePendingReservationsResponse, GetReservationByIdRequest,
    GetReservationByUuidRequest, ReservationDetailItem, ReservationResponse,
    UpdateReservationStatusRequest,
};

#[derive(sqlx::FromRow)]
pub struct ReservationRow {
    pub id: i64,
    pub uuid: String,
    pub client_id: Option<i64>,
    pub client_uuid: Option<String>,
    pub vendor_id: Option<i64>,
    pub discount: Option<f64>,
    pub total: Option<f64>,
    pub receipt_url: Option<String>,
    pub payment_method: Option<String>,
    pub status: String,
    pub account_preference: Option<String>,
    pub expiration_date: String,
    pub created_at: String,
    pub notes: Option<String>,
    pub renewal_subscription_id: Option<i64>,
    pub renewal_subscription_uuid: Option<String>,
    pub points_redeemed: Option<i64>,
    pub points_discount: Option<f64>,
}

#[derive(sqlx::FromRow)]
pub struct DetailRow {
    pub id: i64,
    pub uuid: String,
    pub service_id: i64,
    pub qty: i32,
    pub unit_price: f64,
    pub subtotal: f64,
}

pub struct MyReservationService {
    pub db: DbPool,
}

impl MyReservationService {
    pub fn new(db: DbPool) -> Self {
        Self { db }
    }
}

fn status_enum_to_str(status: i32) -> &'static str {
    match status {
        1 => "pending",
        2 => "uploaded",
        3 => "validated",
        4 => "rejected",
        5 => "expired",
        6 => "cancelled",
        _ => "pending",
    }
}

fn status_str_to_enum_i32(status_str: &str) -> i32 {
    match status_str.to_lowercase().as_str() {
        "pending" => 1,
        "uploaded" => 2,
        "validated" => 3,
        "rejected" => 4,
        "expired" => 5,
        "cancelled" => 6,
        _ => 1,
    }
}

#[tonic::async_trait]
impl ReservationService for MyReservationService {
    async fn create_reservation(
        &self,
        request: Request<CreateReservationRequest>,
    ) -> Result<Response<ReservationResponse>, Status> {
        let req = request.into_inner();
        let reservation_uuid = Uuid::new_v4().to_string();

        println!(
            "Received CreateReservation request for client_id={}, vendor_id={}, total={}",
            req.client_id, req.vendor_id, req.total
        );

        let discount: f64 = req.discount.parse().unwrap_or(0.0);
        let total: f64 = req.total.parse().unwrap_or(0.0);
        let points_discount: f64 = req.points_discount.parse().unwrap_or(0.0);

        // Expiration date = Now + 60 minutes
        let result = sqlx::query(
            r#"
            INSERT INTO reservations 
            (uuid, client_id, client_uuid, vendor_id, discount, total, payment_method, 
             status, account_preference, expiration_date, notes, 
             renewal_subscription_id, renewal_subscription_uuid, points_redeemed, points_discount)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', ?, datetime('now', '+60 minutes'), ?, ?, ?, ?, ?)
            "#,
        )
        .bind(&reservation_uuid)
        .bind(req.client_id)
        .bind(if req.client_uuid.is_empty() { None } else { Some(&req.client_uuid) })
        .bind(req.vendor_id)
        .bind(discount)
        .bind(total)
        .bind(if req.payment_method.is_empty() { None } else { Some(&req.payment_method) })
        .bind(if req.account_preference.is_empty() { None } else { Some(&req.account_preference) })
        .bind(if req.notes.is_empty() { None } else { Some(&req.notes) })
        .bind(if req.renewal_subscription_id == 0 { None } else { Some(req.renewal_subscription_id) })
        .bind(if req.renewal_subscription_uuid.is_empty() { None } else { Some(&req.renewal_subscription_uuid) })
        .bind(req.points_redeemed)
        .bind(points_discount)
        .execute(&self.db)
        .await;

        let reservation_id = match result {
            Ok(res) => res.last_insert_rowid(),
            Err(e) => {
                eprintln!("Failed to insert reservation: {}", e);
                return Ok(Response::new(ReservationResponse {
                    success: false,
                    message: format!("Database insert error: {}", e),
                    ..Default::default()
                }));
            }
        };

        // Insert Details
        let mut detail_items = Vec::new();
        for item in req.details {
            let detail_uuid = Uuid::new_v4().to_string();
            let unit_price: f64 = item.unit_price.parse().unwrap_or(0.0);
            let subtotal = (item.qty as f64) * unit_price;

            let detail_res = sqlx::query(
                r#"
                INSERT INTO reservation_details
                (uuid, reservation_id, service_id, qty, unit_price, subtotal)
                VALUES (?, ?, ?, ?, ?, ?)
                "#,
            )
            .bind(&detail_uuid)
            .bind(reservation_id)
            .bind(item.service_id)
            .bind(item.qty)
            .bind(unit_price)
            .bind(subtotal)
            .execute(&self.db)
            .await;

            if let Ok(dres) = detail_res {
                detail_items.push(ReservationDetailItem {
                    id: dres.last_insert_rowid(),
                    uuid: detail_uuid,
                    service_id: item.service_id,
                    qty: item.qty,
                    unit_price: item.unit_price,
                    subtotal: subtotal.to_string(),
                });
            }
        }

        // Fetch inserted record timestamps
        let row: Option<(String, String)> = sqlx::query_as(
            "SELECT expiration_date, created_at FROM reservations WHERE id = ?",
        )
        .bind(reservation_id)
        .fetch_optional(&self.db)
        .await
        .unwrap_or(None);

        let (expiration_date, created_at) = row.unwrap_or_default();

        Ok(Response::new(ReservationResponse {
            success: true,
            message: "Reservation created successfully".to_string(),
            id: reservation_id,
            uuid: reservation_uuid,
            client_id: req.client_id,
            client_uuid: req.client_uuid,
            vendor_id: req.vendor_id,
            discount: req.discount,
            total: req.total,
            receipt_url: String::new(),
            payment_method: req.payment_method,
            status: 1, // PENDING
            account_preference: req.account_preference,
            expiration_date,
            created_at,
            notes: req.notes,
            renewal_subscription_id: req.renewal_subscription_id,
            renewal_subscription_uuid: req.renewal_subscription_uuid,
            details: detail_items,
            points_redeemed: req.points_redeemed,
            points_discount: req.points_discount,
        }))
    }

    async fn get_reservation_by_uuid(
        &self,
        request: Request<GetReservationByUuidRequest>,
    ) -> Result<Response<ReservationResponse>, Status> {
        let req = request.into_inner();

        let row: Option<ReservationRow> = sqlx::query_as(
            r#"
            SELECT id, uuid, client_id, client_uuid, vendor_id,
                   discount, total, receipt_url, payment_method, status,
                   account_preference, expiration_date, created_at, notes, renewal_subscription_id,
                   renewal_subscription_uuid, points_redeemed, points_discount
            FROM reservations WHERE uuid = ?
            "#,
        )
        .bind(&req.uuid)
        .fetch_optional(&self.db)
        .await
        .map_err(|e| Status::internal(format!("Query error: {}", e)))?;

        match row {
            Some(r) => {
                let details_rows: Vec<DetailRow> = sqlx::query_as(
                    "SELECT id, uuid, service_id, qty, unit_price, subtotal FROM reservation_details WHERE reservation_id = ?"
                )
                .bind(r.id)
                .fetch_all(&self.db)
                .await
                .unwrap_or_default();

                let details = details_rows
                    .into_iter()
                    .map(|d| ReservationDetailItem {
                        id: d.id,
                        uuid: d.uuid,
                        service_id: d.service_id,
                        qty: d.qty,
                        unit_price: d.unit_price.to_string(),
                        subtotal: d.subtotal.to_string(),
                    })
                    .collect();

                Ok(Response::new(ReservationResponse {
                    success: true,
                    message: "Reservation found".to_string(),
                    id: r.id,
                    uuid: r.uuid,
                    client_id: r.client_id.unwrap_or_default(),
                    client_uuid: r.client_uuid.unwrap_or_default(),
                    vendor_id: r.vendor_id.unwrap_or_default(),
                    discount: r.discount.unwrap_or_default().to_string(),
                    total: r.total.unwrap_or_default().to_string(),
                    receipt_url: r.receipt_url.unwrap_or_default(),
                    payment_method: r.payment_method.unwrap_or_default(),
                    status: status_str_to_enum_i32(&r.status),
                    account_preference: r.account_preference.unwrap_or_default(),
                    expiration_date: r.expiration_date,
                    created_at: r.created_at,
                    notes: r.notes.unwrap_or_default(),
                    renewal_subscription_id: r.renewal_subscription_id.unwrap_or_default(),
                    renewal_subscription_uuid: r.renewal_subscription_uuid.unwrap_or_default(),
                    details,
                    points_redeemed: r.points_redeemed.unwrap_or_default(),
                    points_discount: r.points_discount.unwrap_or_default().to_string(),
                }))
            }
            None => Ok(Response::new(ReservationResponse {
                success: false,
                message: "Reservation not found".to_string(),
                ..Default::default()
            })),
        }
    }

    async fn get_reservation_by_id(
        &self,
        request: Request<GetReservationByIdRequest>,
    ) -> Result<Response<ReservationResponse>, Status> {
        let req = request.into_inner();

        let uuid_row: Option<(String,)> = sqlx::query_as("SELECT uuid FROM reservations WHERE id = ?")
            .bind(req.id)
            .fetch_optional(&self.db)
            .await
            .map_err(|e| Status::internal(format!("Query error: {}", e)))?;

        match uuid_row {
            Some((uuid,)) => {
                self.get_reservation_by_uuid(Request::new(GetReservationByUuidRequest { uuid })).await
            }
            None => Ok(Response::new(ReservationResponse {
                success: false,
                message: "Reservation not found".to_string(),
                ..Default::default()
            })),
        }
    }

    async fn update_reservation_status(
        &self,
        request: Request<UpdateReservationStatusRequest>,
    ) -> Result<Response<ReservationResponse>, Status> {
        let req = request.into_inner();
        let status_str = status_enum_to_str(req.status);

        println!(
            "Updating status for reservation uuid={} to status={}",
            req.uuid, status_str
        );

        let result = sqlx::query(
            r#"
            UPDATE reservations
            SET status = ?,
                receipt_url = COALESCE(NULLIF(?, ''), receipt_url),
                notes = COALESCE(NULLIF(?, ''), notes)
            WHERE uuid = ?
            "#,
        )
        .bind(status_str)
        .bind(&req.receipt_url)
        .bind(&req.notes)
        .bind(&req.uuid)
        .execute(&self.db)
        .await;

        match result {
            Ok(res) => {
                if res.rows_affected() > 0 {
                    self.get_reservation_by_uuid(Request::new(GetReservationByUuidRequest { uuid: req.uuid })).await
                } else {
                    Ok(Response::new(ReservationResponse {
                        success: false,
                        message: "Reservation not found for update".to_string(),
                        ..Default::default()
                    }))
                }
            }
            Err(e) => Ok(Response::new(ReservationResponse {
                success: false,
                message: format!("Update error: {}", e),
                ..Default::default()
            })),
        }
    }

    async fn check_active_renewal(
        &self,
        request: Request<CheckActiveRenewalRequest>,
    ) -> Result<Response<CheckActiveRenewalResponse>, Status> {
        let req = request.into_inner();

        let row: Option<(i64,)> = sqlx::query_as(
            r#"
            SELECT 1 FROM reservations
            WHERE renewal_subscription_id = ?
              AND status IN ('pending', 'uploaded')
            LIMIT 1
            "#,
        )
        .bind(req.subscription_id)
        .fetch_optional(&self.db)
        .await
        .map_err(|e| Status::internal(format!("CheckActiveRenewal query failed: {}", e)))?;

        let active = row.is_some();
        Ok(Response::new(CheckActiveRenewalResponse { active }))
    }

    async fn expire_pending_reservations(
        &self,
        request: Request<ExpirePendingReservationsRequest>,
    ) -> Result<Response<ExpirePendingReservationsResponse>, Status> {
        let req = request.into_inner();
        let mins = if req.expiration_minutes <= 0 { 60 } else { req.expiration_minutes };

        let result = sqlx::query(
            &format!(
                r#"
                UPDATE reservations 
                SET status = 'expired' 
                WHERE status = 'pending' 
                  AND datetime(expiration_date) <= datetime('now', '-{} minutes')
                "#,
                mins - 60
            ),
        )
        .execute(&self.db)
        .await;

        match result {
            Ok(res) => {
                let count = res.rows_affected() as i32;
                Ok(Response::new(ExpirePendingReservationsResponse {
                    expired_count: count,
                }))
            }
            Err(e) => Err(Status::internal(format!("Expiration query failed: {}", e))),
        }
    }
}
