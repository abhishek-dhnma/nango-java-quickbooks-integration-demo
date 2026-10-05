# Nango + QuickBooks Online + Spring Boot: An Enterprise Java Reference Implementation

> **A 100% live-verified reference architecture and technical guide demonstrating how enterprise Java and Spring Boot engineering teams build resilient multi-tenant SaaS accounting integrations using Nango's REST API.**

---

## 1. Why This Project Exists

Most integration content and tutorials in the developer ecosystem focus on Node.js / TypeScript developers building AI chatbots or Slack bots.

However, mission-critical ERP, accounting, invoicing, and subscription ledgers at B2B and mid-market SaaS companies are overwhelmingly built on **Java and Spring Boot**. When an enterprise team evaluates Nango for financial integrations like **QuickBooks Online**, they encounter an immediate reality:

1. **Nango's Java SDK is marked "Coming soon"**: The official documentation recommends using Nango's REST API directly.
2. **Multi-Tenant Complexity**: Accounting platforms like Intuit QuickBooks require strict multi-tenant authorization, company ID (`realmId`) scoping, and continuous token lifecycle management.
3. **Enterprise Resilience**: Java backends require typed data structures, robust HTTP error translation (RFC 7807 `ProblemDetail`), rate limit backoff handling, and HMAC webhook verification without stream consumption bugs.

This reference implementation answers the core question:  
**What does a production-ready, idiomatic Spring Boot integration look like when consuming Nango's REST API against live QuickBooks Online data?**

---

## 2. 100% Live-Verified Architecture

Unlike mock-only tutorials or theoretical design documents, **every component of this project has been executed and verified live against active cloud infrastructure**:

* **Live Identity Provider**: Intuit Developer Platform (`developer.intuit.com`) via OAuth 2.0.
* **Live Target System**: QuickBooks Online Sandbox company (`Sandbox Company US c56a`, Realm ID: `9341458413053036`).
* **Live Integration Gateway**: Nango Cloud (`api.nango.dev`), managing token vaults, automatic OAuth refresh, and dynamic request proxying.
* **Live Backend**: Spring Boot 3.3.4 running Java 17, consuming Nango via modern Spring 6 `RestClient`.

```
[ Frontend / Tenant Admin ]
           │
           │ 1. POST /api/tenants/{id}/accounting/connect
           ▼
┌──────────────────────────────────────────────┐
│       Spring Boot 3.3 Backend                │
│                                              │
│  - TenantAccountingController                │
│  - QuickBooksAccountingService               │
│  - Idiomatic NangoClient (Spring RestClient) │
│  - RFC 7807 GlobalIntegrationExceptionHandler│
│  - WebhookContentCachingFilter (HMAC-SHA256) │
└──────────────────────┬───────────────────────┘
                       │
                       │ 2. Authenticated REST calls (Bearer NANGO_SECRET_KEY)
                       ▼
┌──────────────────────────────────────────────┐
│           Nango Cloud Platform               │
│                                              │
│  - OAuth 2.0 Credential Vault & Refresh      │
│  - Connect Session Service (/connect/sessions)│
│  - Requests Proxy (/proxy/v3/company/...)    │
│  - Signature-verified Webhooks               │
└──────────────────────┬───────────────────────┘
                       │
                       │ 3. Injected Bearer Token & Realm ID Scoping
                       ▼
┌──────────────────────────────────────────────┐
│         Intuit QuickBooks Online             │
│        (Sandbox Company US c56a)             │
│                                              │
│  - Company Metadata API                      │
│  - Customer Ledger Query API                 │
│  - Invoice & Line-Item Query API             │
└──────────────────────────────────────────────┘
```

---

## 3. Key Technical Challenges & Solutions

### 1. Modern Spring 6 `RestClient` for Nango's REST API
Because Nango's Java SDK is marked "Coming soon", this project provides a clean, typed `NangoClient` utilizing Spring Boot 3's modern `RestClient`. It encapsulates:
* Automatic `Bearer <secret-key>` injection.
* Clean proxy forwarding with `Connection-Id` and `Provider-Config-Key` headers.
* Strongly-typed Java records for request payloads and responses (`QuickBooksInvoice`, `QuickBooksCustomer`, `QuickBooksCompanyInfo`).

### 2. Multi-Tenant Company (`realmId`) Resolution
QuickBooks Online endpoints require the target company's `realmId` in the path (e.g. `v3/company/{realmId}/query`).
During the OAuth handshake, Nango automatically stores this in the connection configuration:
```json
{
  "connection_id": "c5a55682-b9f5-4798-a1a0-55dbdb7f7e4b",
  "provider_config_key": "quickbooks-sandbox",
  "connection_config": {
    "realmId": "9341458413053036"
  }
}
```
Our `QuickBooksAccountingServiceImpl` dynamically resolves and caches the `realmId` per tenant connection, preventing hardcoded company IDs and maintaining strict tenant isolation.

### 3. Java Exception Mapping to RFC 7807 ProblemDetail
Nango returns standard HTTP error codes:
* `404 Not Found` (when a connection has not been authorized)
* `424 Failed Dependency` (when the upstream integration provider rejects a call)
* `429 Too Many Requests` (when provider or Nango rate limits are hit, carrying `Retry-After`)

Our `NangoResponseErrorHandler` and `GlobalIntegrationExceptionHandler` map these upstream statuses directly into RFC 7807 `ProblemDetail` structures compliant with modern enterprise API design.

### 4. Servlet Stream Re-readability in Webhook HMAC Verification
Cryptographic verification of Nango webhooks requires hashing the exact raw request bytes with HMAC-SHA256. In a standard Spring Boot application, reading the `HttpServletRequest.getInputStream()` consumes the stream, causing downstream Jackson deserializers to fail with `HttpMessageNotReadableException`.

We solved this using a custom `WebhookContentCachingFilter` and Spring's `ContentCachingRequestWrapper`, enabling signature verification and JSON body parsing on the same request.

---

## 4. Live API Walkthrough & Verification

All commands below run against the live, running Spring Boot server connected to Nango Cloud and QuickBooks Online Sandbox:

### A. Generate Hosted Nango Connect Link for a Tenant
Initiates a new authorization session with an explicit `end_user` payload:
```bash
curl -X POST http://localhost:8080/api/tenants/tenant-2/accounting/connect
```
**Response**:
```json
{
  "token": "nango_connect_session_f7b47622c5760da26c9d0e6de4832efc76e43e6cc66f4933d7bb59e84b7325ea",
  "connect_link": "https://connect.nango.dev/?session_token=nango_connect_session_f7b47622c5760da26c9d0e6de4832efc76e43e6cc66f4933d7bb59e84b7325ea",
  "expires_at": "2026-10-03T14:59:17.954Z"
}
```

### B. Fetch Live Company Info via Nango Proxy
Calls `/proxy/v3/company/{realmId}/companyinfo/{realmId}`:
```bash
curl http://localhost:8080/api/tenants/tenant-1/accounting/company
```
**Response**:
```json
{
  "CompanyName": "Sandbox Company US c56a",
  "LegalName": "Sandbox Company US c56a",
  "CompanyAddr": {
    "Line1": "123 Sierra Way",
    "City": "San Pablo",
    "CountrySubDivisionCode": "CA",
    "PostalCode": "87999"
  },
  "Country": "US",
  "FiscalYearStartMonth": "January"
}
```

### C. Fetch Live Invoices via Nango Proxy Query
Executes a live query against QuickBooks Online (`select * from Invoice maxresults 2`):
```bash
curl "http://localhost:8080/api/tenants/tenant-1/accounting/invoices?limit=2"
```
**Response**:
```json
[
  {
    "Id": "130",
    "DocNumber": "1037",
    "TxnDate": "2026-09-06",
    "DueDate": "2026-10-06",
    "TotalAmt": 362.07,
    "Balance": 362.07,
    "CustomerRef": {
      "value": "24",
      "name": "Sonnenschein Family Store"
    },
    "BillEmail": {
      "Address": "Familiystore@intuit.com"
    },
    "Line": [
      {
        "Id": "1",
        "LineNum": 1,
        "Description": "Rock Fountain",
        "Amount": 275.00,
        "DetailType": "SalesItemLineDetail"
      },
      {
        "Id": "2",
        "LineNum": 2,
        "Description": "Fountain Pump",
        "Amount": 12.75,
        "DetailType": "SalesItemLineDetail"
      }
    ]
  }
]
```

### D. Fetch Live Customers
Executes a live query against QuickBooks Online (`select * from Customer maxresults 2`):
```bash
curl "http://localhost:8080/api/tenants/tenant-1/accounting/customers?limit=2"
```
**Response**:
```json
[
  {
    "Id": "8",
    "DisplayName": "0969 Ocean View Road",
    "GivenName": "Sasha",
    "FamilyName": "Tillou",
    "CompanyName": "Freeman Sporting Goods",
    "Active": true,
    "Balance": 477.5,
    "PrimaryEmailAddr": {
      "Address": "Sporting_goods@intuit.com"
    },
    "PrimaryPhone": {
      "FreeFormNumber": "(415) 555-9933"
    }
  }
]
```

---

## 5. Automated Test Suite

The test suite covers contract accuracy, HMAC signature verification, and HTTP error handling without relying on external network calls during CI:

```bash
mvn test
```

### Test Results
```
[INFO] Running com.example.nango.NangoClientIntegrationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.nango.NangoWebhookVerificationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.nango.TenantAccountingControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS (12 tests total, 0 failures)
```

---

## 6. How to Run Locally

### Prerequisites
- Java 17 or higher
- Maven 3.6.3+ (or Maven 3.9+)
- A Nango account (`app.nango.dev`) with a configured `quickbooks-sandbox` integration
- Intuit Developer sandbox account

### Environment Configuration
Copy `.env.example` to `.env` and populate your secrets:
```bash
cp .env.example .env
```
*(The `.env` file is git-ignored and will never be committed to source control).*

### Launch

**On Windows (PowerShell)**:
```powershell
# Automatically loads .env and starts the server:
.\run.ps1

# Or run tests:
.\run.ps1 test
```

**On Linux / macOS**:
```bash
export $(cat .env | xargs)
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.
