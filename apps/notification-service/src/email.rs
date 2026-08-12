pub async fn dispatch_email(
    client: &reqwest::Client,
    api_key: &str,
    from_email: &str,
    recipient: &str,
    event_type: &str,
    payload_json: &str,
) -> Result<String, String> {
    if api_key == "re_placeholder" || api_key.is_empty() {
        let msg = format!(
            "Mock mode: email to {} for event {} (RESEND_API_KEY is placeholder)",
            recipient, event_type
        );
        println!("{}", msg);
        return Ok(msg);
    }

    let parsed_payload: serde_json::Value = serde_json::from_str(payload_json)
        .unwrap_or_else(|_| serde_json::json!({ "rawPayload": payload_json }));

    let template_id = get_template_id_for_event(event_type);

    let body = serde_json::json!({
        "from": from_email,
        "to": [recipient],
        "template": {
            "id": template_id,
            "variables": parsed_payload
        }
    });

    println!("Sending email via Resend to {} for event {}...", recipient, event_type);

    let res = client
        .post("https://api.resend.com/emails")
        .bearer_auth(api_key)
        .json(&body)
        .send()
        .await
        .map_err(|e| format!("HTTP request failed: {}", e))?;

    let status = res.status();
    if status.is_success() {
        let text = res.text().await.unwrap_or_default();
        println!("Email successfully dispatched via Resend: {}", text);
        Ok(format!("Email sent successfully via Resend: {}", text))
    } else {
        let err_text = res.text().await.unwrap_or_default();
        Err(format!("Resend delivery failed (status {}): {}", status, err_text))
    }
}

pub fn get_template_id_for_event(event_type: &str) -> &str {
    match event_type {
        "CLIENT_WELCOME" | "CLIENT_REGISTRATION" => "client-welcome",
        "VENDOR_WELCOME" => "vendor-welcome",
        "PAYMENT_APPROVED" => "payment-approved",
        "RECEIPT_UPLOADED" => "receipt-uploaded",
        "VENDOR_RECEIPT_UPLOADED" => "vendor-receipt-uploaded",
        "RECEIPT_REJECTED" => "receipt-rejected",
        "ACCESS_DELIVERED" => "access-delivered",
        "ACCESS_REVOKED" => "access-revoked",
        "SUBSCRIPTION_RENEWED" => "subscription-renewed",
        "SUBSCRIPTION_EXPIRED" => "subscription-expired",
        "RENEWAL_REMINDER_7D" => "renewal-reminder-7d",
        "RENEWAL_REMINDER_3D" => "renewal-reminder-3d",
        "RENEWAL_REMINDER_1D" => "renewal-reminder-1d",
        "ACCOUNT_RENEWAL_REMINDER_7D" => "account-renewal-reminder-7d",
        "ACCOUNT_RENEWAL_REMINDER_3D" => "account-renewal-reminder-3d",
        "ACCOUNT_RENEWAL_REMINDER_1D" => "account-renewal-reminder-1d",
        "ACCOUNT_RENEWAL_REMINDER_DUE" => "account-renewal-reminder-due",
        "ORDER_COMPLETED" => "order-completed",
        "ORDER_CANCELLED" => "order-cancelled",
        _ => "default-notification",
    }
}
