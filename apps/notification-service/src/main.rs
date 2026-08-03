use std::env;
use tonic::transport::Server;

mod db;
mod email;
mod service;

use service::proto::notification_service_server::NotificationServiceServer;
use service::MyNotificationService;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    dotenvy::dotenv().ok();

    let port = env::var("PORT").unwrap_or_else(|_| "50051".to_string());
    let addr = format!("0.0.0.0:{}", port).parse()?;

    let resend_api_key = env::var("RESEND_API_KEY").unwrap_or_else(|_| "re_placeholder".to_string());
    let from_email = env::var("FROM_EMAIL").unwrap_or_else(|_| "Neversion <noreply@mail.neversion.com>".to_string());

    println!("Initializing SQLite database connection...");
    let pool = db::init_db().await?;

    let notification_service = MyNotificationService::new(pool, resend_api_key, from_email);

    println!("gRPC Notification Service is running on port {}", port);

    Server::builder()
        .add_service(NotificationServiceServer::new(notification_service))
        .serve(addr)
        .await?;

    Ok(())
}
