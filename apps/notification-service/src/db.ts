import Database from 'better-sqlite3';
import dotenv from 'dotenv';
import path from 'path';

dotenv.config();

const dbPath = process.env.DATABASE_PATH || 'notifications.db';
const absoluteDbPath = path.isAbsolute(dbPath) ? dbPath : path.resolve(process.cwd(), dbPath);

console.log(`Connecting to SQLite database at: ${absoluteDbPath}`);
const db = new Database(absoluteDbPath);

// Initialize schema
db.exec(`
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
`);

export default db;
