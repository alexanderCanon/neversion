use std::env;
use tonic::transport::Server;

mod db;
mod service;
mod worker;

use service::proto::reservation_service_server::ReservationServiceServer;
use service::MyReservationService;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    dotenvy::dotenv().ok();

    let port = env::var("PORT").unwrap_or_else(|_| "50052".to_string());
    let addr = format!("0.0.0.0:{}", port).parse()?;

    println!("Initializing SQLite database for reservation-service...");
    let pool = db::init_db().await?;

    // Start background Tokio expiration worker (BR-01) checking every 60 seconds
    let check_interval: u64 = env::var("EXPIRATION_CHECK_INTERVAL_SECS")
        .unwrap_or_else(|_| "60".to_string())
        .parse()
        .unwrap_or(60);

    worker::start_expiration_worker(pool.clone(), check_interval);

    let reservation_service = MyReservationService::new(pool);

    println!("gRPC Reservation Service is running on port {}", port);

    Server::builder()
        .add_service(ReservationServiceServer::new(reservation_service))
        .serve(addr)
        .await?;

    Ok(())
}
