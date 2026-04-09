# Http responses and handling errors

This document establishes the standard for HTTP responses and error handling in Neversion APIs, following the principles defined in `api-architecture.md`.

---

## 1. Successful Responses

Following the principle of **pure REST**, responses should send the resource data directly.

**Golden rule:** Avoid generic wrappers (e.g., `{"success": true, "data": {...}}`) because they break HTTP semantics.

### Common Codes
- **`200 OK`**: For queries (`GET`), updates (`PUT`/`PATCH`) or generic successful actions.
- **`201 Created`**: For resource creation (`POST`). Should ideally include the created resource or a `Location` header with the new resource's URI.
- **`204 No Content`**: For successful deletions (`DELETE`) or actions that do not require returning a body in the response.

**Example of REST Response (`200 OK`):**
```json
{
  "orderId": "123",
  "status": "PENDING"
}
```

---

## 2. Error Handling (Problem Details)

All errors must follow the **RFC 7807 (Problem Details for HTTP APIs)** standard. This ensures consistency and interoperability between the frontend panel and backend microservices.

### Base Structure

```json
{
  "type": "https://api.neversion.com/errors/bad-request",
  "title": "Bad Request",
  "status": 400,
  "detail": "Data validation has failed.",
  "instance": "/api/v1/orders/123"
}
```

- **`type`**: URI that identifies the type of problem (ideally points to documentation about the error).
- **`title`**: Short summary in human-readable text.
- **`status`**: HTTP status code (must match the HTTP header).
- **`detail`**: Detailed explanation specific to this exact error.
- **`instance`**: URI of the exact resource that caused the error.

### Error Extension (Cross-field Validations)
The RFC 7807 allows adding additional properties. For validation errors (`400 Bad Request`), a `fieldErrors` list must be included:

```json
{
  "type": "https://api.neversion.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "There are errors in the submitted fields.",
  "instance": "/api/v1/orders",
  "fieldErrors": [
    {
      "field": "price",
      "message": "The price cannot be negative",
      "rejectedValue": -50
    }
  ]
}
```

---

## 3. Error Status Codes (4xx and 5xx)

The following codes are the standard to use according to the type of exception or failure:

### `400 Bad Request`
- **Use:** Invalid input data, malformed JSON, incorrect data types, field validations (`@Valid`).
- **Response:** Error detail, ideally accompanied by the `fieldErrors` array.

### `401 Unauthorized`
- **Use:** The user is not authenticated, there is no JWT token, or the token is invalid/expired.
- **Response:** Indicate that the session has expired or credentials are missing.

### `403 Forbidden`
- **Use:** The user is authenticated but *does not have permissions* (Roles) to perform the requested action.
- **Response:** Indicate that the necessary privileges are not possessed.

### `404 Not Found`
- **Use:** The requested resource does not exist (`ResourceNotFoundException`).
- **Response:** Detail which entity was not found.

### `409 Conflict`
- **Critical Use:** Business rule violations (`BusinessRuleException`) or database integrity problems (`DataIntegrityViolationException`, such as duplicate records).
- **Response:** Clearly explain why the action conflicts with the current system state (e.g., "The account already has an active subscription").

### `500 Internal Server Error`
- **Use:** Uncontrolled errors, database crashes, generic Java exceptions.
- **Generic Response:** *Do not* expose StackTraces to the client. Respond only with "Internal server error". (Details should remain in application logs using `ErrorLogger`).

---

## 4. Future Considerations (Technical Debt)

> [!NOTE]
> **Standardization of the `type` field (RFC 7807):**
> Currently, the backend implementation returns simple strings to identify the error (e.g., `"Conflict"`, `"Bad Request"`) instead of full canonical URIs. In the future, the use of a real and resolvable domain or URN will be standardized (for example, `https://api.neversion.com/errors/business-rule-violation`) to strictly comply with the URL format required by the RFC 7807 standard.
