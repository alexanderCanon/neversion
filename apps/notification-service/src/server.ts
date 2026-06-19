import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import path from 'path';
import dotenv from 'dotenv';
import { v4 as uuidv4 } from 'uuid';
import db from './db';
import { resolveTemplate } from './templates';
import { Resend } from 'resend';

dotenv.config();

const PROTO_PATH = path.resolve(__dirname, '../proto/notification.proto');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const protoDescriptor = grpc.loadPackageDefinition(packageDefinition) as any;
const notificationProto = protoDescriptor.neversion.notification;

const resendApiKey = process.env.RESEND_API_KEY || 're_placeholder';
const fromEmail = process.env.FROM_EMAIL || 'Neversion <noreply@neversion.com>';
const resend = new Resend(resendApiKey);

// Internal-only notification types that should NOT trigger an email
const INTERNAL_TYPES = new Set([
  'NO_INVENTORY_ALERT',
  'SUBSCRIPTIONS_EXPIRED_DAILY'
]);

/**
 * Handles SendNotification RPC
 */
const sendNotification = async (call: any, callback: any) => {
  const req = call.request;
  const { event_type, recipient, payload, reference_type, reference_id, tags } = req;

  console.log(`Received SendNotification request: event_type=${event_type}, recipient=${recipient}, reference_type=${reference_type}, reference_id=${reference_id}, tags=${tags}`);

  const notificationUuid = uuidv4();
  const createdAt = new Date().toISOString();

  // 1. Insert log into SQLite as 'pending'
  try {
    const insertStmt = db.prepare(`
      INSERT INTO notification_logs 
      (uuid, event_type, recipient, payload, reference_type, reference_id, tags, status, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);
    insertStmt.run(
      notificationUuid,
      event_type,
      recipient,
      payload || null,
      reference_type || null,
      reference_id ? Number(reference_id) : null,
      tags || null,
      'pending',
      createdAt
    );
  } catch (err: any) {
    console.error(`Failed to write pending log to SQLite: ${err.message}`);
    return callback(null, {
      success: false,
      message: `Failed to initialize log: ${err.message}`,
      notification_uuid: ''
    });
  }

  // Check if internal-only
  if (INTERNAL_TYPES.has(event_type)) {
    console.log(`Event ${event_type} is internal-only. Skipping email dispatch.`);
    try {
      db.prepare(`
        UPDATE notification_logs 
        SET status = 'success', processed_at = ?
        WHERE uuid = ?
      `).run(new Date().toISOString(), notificationUuid);

      return callback(null, {
        success: true,
        message: 'Internal notification recorded (email skipped)',
        notification_uuid: notificationUuid
      });
    } catch (err: any) {
      console.error(`Failed to update internal log: ${err.message}`);
    }
  }

  // 2. Resolve template and render HTML
  let subject = 'Notificación';
  let htmlBody = '';
  try {
    const spec = resolveTemplate(event_type, payload);
    subject = spec.subject;
    htmlBody = spec.htmlBody;
  } catch (err: any) {
    const errMsg = `Template resolution failed: ${err.message}`;
    console.error(errMsg);
    try {
      db.prepare(`
        UPDATE notification_logs 
        SET status = 'failed', error_message = ?, processed_at = ?
        WHERE uuid = ?
      `).run(errMsg, new Date().toISOString(), notificationUuid);
    } catch (dbErr: any) {
      console.error(`Failed to update log state: ${dbErr.message}`);
    }

    return callback(null, {
      success: false,
      message: errMsg,
      notification_uuid: notificationUuid
    });
  }

  // 3. Dispatch email via Resend
  if (resendApiKey === 're_placeholder') {
    // Local fallback or mock mode when no API key is set
    const mockMsg = `Mock mode: email to ${recipient} with subject "${subject}" (RESEND_API_KEY is placeholder)`;
    console.log(mockMsg);
    try {
      db.prepare(`
        UPDATE notification_logs 
        SET status = 'success', processed_at = ?
        WHERE uuid = ?
      `).run(new Date().toISOString(), notificationUuid);

      return callback(null, {
        success: true,
        message: mockMsg,
        notification_uuid: notificationUuid
      });
    } catch (dbErr: any) {
      console.error(`Failed to update log status: ${dbErr.message}`);
    }
  } else {
    try {
      console.log(`Sending email via Resend to ${recipient}...`);
      const response = await resend.emails.send({
        from: fromEmail,
        to: recipient,
        subject: subject,
        html: htmlBody,
      });

      if (response.error) {
        throw new Error(response.error.message || JSON.stringify(response.error));
      }

      console.log(`Email successfully dispatched via Resend, id: ${response.data?.id}`);
      
      // Update status to success
      db.prepare(`
        UPDATE notification_logs 
        SET status = 'success', processed_at = ?
        WHERE uuid = ?
      `).run(new Date().toISOString(), notificationUuid);

      return callback(null, {
        success: true,
        message: 'Email sent successfully',
        notification_uuid: notificationUuid
      });
    } catch (err: any) {
      const errMsg = `Resend delivery failed: ${err.message}`;
      console.error(errMsg);

      try {
        db.prepare(`
          UPDATE notification_logs 
          SET status = 'failed', error_message = ?, processed_at = ?
          WHERE uuid = ?
        `).run(errMsg.substring(0, 500), new Date().toISOString(), notificationUuid);
      } catch (dbErr: any) {
        console.error(`Failed to update failure log: ${dbErr.message}`);
      }

      return callback(null, {
        success: false,
        message: errMsg,
        notification_uuid: notificationUuid
      });
    }
  }
};

/**
 * Handles CheckExists RPC
 */
const checkExists = (call: any, callback: any) => {
  const req = call.request;
  const { reference_type, reference_id, tags } = req;

  console.log(`Received CheckExists request: reference_type=${reference_type}, reference_id=${reference_id}, tags=${tags}`);

  try {
    const row = db.prepare(`
      SELECT 1 FROM notification_logs 
      WHERE reference_type = ? AND reference_id = ? AND tags = ? AND status = 'success'
      LIMIT 1
    `).get(reference_type, Number(reference_id), tags);

    const exists = !!row;
    console.log(`CheckExists result: ${exists}`);
    return callback(null, { exists });
  } catch (err: any) {
    console.error(`CheckExists query failed: ${err.message}`);
    return callback(err);
  }
};

/**
 * Starts gRPC Server
 */
const main = () => {
  const server = new grpc.Server();
  server.addService(notificationProto.NotificationService.service, {
    SendNotification: sendNotification,
    CheckExists: checkExists,
  });

  const port = process.env.PORT || '50051';
  const address = `0.0.0.0:${port}`;

  server.bindAsync(address, grpc.ServerCredentials.createInsecure(), (err, boundPort) => {
    if (err) {
      console.error(`Failed to bind server: ${err.message}`);
      process.exit(1);
    }
    console.log(`gRPC Notification Service is running on port ${boundPort}`);
  });
};

main();
