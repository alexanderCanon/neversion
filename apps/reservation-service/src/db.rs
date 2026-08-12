use sqlx::{sqlite::SqlitePoolOptions, Pool, Sqlite};
use std::env;

pub type DbPool = Pool<Sqlite>;

pub async fn init_db() -> Result<DbPool, Box<dyn std::error::Error>> {
    let db_path = env::var("DATABASE_PATH").unwrap_or_else(|_| "reservations.db".to_string());
    let connection_string = format!("sqlite:{}?mode=rwc", db_path);
    
    println!("Connecting to SQLite database at: {}", connection_string);

    let pool = SqlitePoolOptions::new()
        .max_connections(5)
        .connect(&connection_string)
        .await?;

    // Enable foreign keys
    sqlx::query("PRAGMA foreign_keys = ON;").execute(&pool).await?;

    // Create tables and indexes
    sqlx::query(
        r#"
        CREATE TABLE IF NOT EXISTS reservations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            uuid TEXT NOT NULL UNIQUE,
            client_id INTEGER,
            client_uuid TEXT,
            vendor_id INTEGER,
            discount REAL,
            total REAL,
            receipt_url TEXT UNIQUE,
            payment_method TEXT,
            status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'uploaded', 'validated', 'rejected', 'expired', 'cancelled')),
            account_preference TEXT,
            expiration_date TEXT NOT NULL,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            notes TEXT,
            renewal_subscription_id INTEGER,
            renewal_subscription_uuid TEXT,
            points_redeemed INTEGER DEFAULT 0,
            points_discount REAL DEFAULT 0.0
        );

        CREATE UNIQUE INDEX IF NOT EXISTS idx_reservations_uuid ON reservations(uuid);
        CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
        CREATE INDEX IF NOT EXISTS idx_reservations_client_id ON reservations(client_id);
        CREATE INDEX IF NOT EXISTS idx_reservations_renewal ON reservations(renewal_subscription_id);

        CREATE TABLE IF NOT EXISTS reservation_details (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            uuid TEXT NOT NULL UNIQUE,
            reservation_id INTEGER NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
            service_id INTEGER NOT NULL,
            qty INTEGER NOT NULL DEFAULT 1,
            unit_price REAL NOT NULL,
            subtotal REAL NOT NULL,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP
        );

        CREATE UNIQUE INDEX IF NOT EXISTS idx_reservation_details_uuid ON reservation_details(uuid);
        CREATE INDEX IF NOT EXISTS idx_reservation_details_reservation ON reservation_details(reservation_id);
        "#,
    )
    .execute(&pool)
    .await?;

    Ok(pool)
}
