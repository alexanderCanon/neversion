use sqlx::{sqlite::SqlitePoolOptions, Pool, Sqlite};
use std::env;

pub type DbPool = Pool<Sqlite>;

pub async fn init_db() -> Result<DbPool, Box<dyn std::error::Error>> {
    let db_path = env::var("DATABASE_PATH").unwrap_or_else(|_| "notifications.db".to_string());
    let connection_string = format!("sqlite:{}?mode=rwc", db_path);
    
    println!("Connecting to SQLite database at: {}", connection_string);

    let pool = SqlitePoolOptions::new()
        .max_connections(5)
        .connect(&connection_string)
        .await?;

    // Create table and indexes if not exists
    sqlx::query(
        r#"
        CREATE TABLE IF NOT EXISTS notification_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            uuid TEXT NOT NULL UNIQUE,
            event_type TEXT NOT NULL,
            recipient TEXT NOT NULL,
            payload TEXT,
            reference_type TEXT,
            reference_id INTEGER,
            tags TEXT,
            status TEXT NOT NULL,
            error_message TEXT,
            processed_at TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP
        );

        CREATE INDEX IF NOT EXISTS idx_ref_tags 
        ON notification_logs(reference_type, reference_id, tags);
        "#,
    )
    .execute(&pool)
    .await?;

    Ok(pool)
}
