use std::time::Duration;
use tokio::time::sleep;

use crate::db::DbPool;

pub fn start_expiration_worker(db: DbPool, check_interval_secs: u64) {
    tokio::spawn(async move {
        println!(
            "Starting reservation expiration background worker (checking every {}s)...",
            check_interval_secs
        );

        loop {
            sleep(Duration::from_secs(check_interval_secs)).await;

            let result = sqlx::query(
                r#"
                UPDATE reservations 
                SET status = 'expired' 
                WHERE status = 'pending' 
                  AND datetime(expiration_date) <= datetime('now')
                "#,
            )
            .execute(&db)
            .await;

            match result {
                Ok(res) => {
                    let count = res.rows_affected();
                    if count > 0 {
                        println!(
                            "BR-01: Expired {} pending reservation(s) exceeding 60-minute window",
                            count
                        );
                    }
                }
                Err(e) => {
                    eprintln!("Error running reservation expiration worker: {}", e);
                }
            }
        }
    });
}
