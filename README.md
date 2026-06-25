# Gmail Rule Engine — Spring Boot + AI Classification

A production-ready Spring Boot webhook server that receives Gmail emails from Zapier,
classifies them using FREE AI, and routes them through a configurable rule engine.

---

## Project Structure

```
gmail-rule-engine/
├── src/main/java/com/ruleengine/
│   ├── RuleEngineApplication.java          ← Spring Boot entry point
│   ├── ai/
│   │   └── AIClassifierService.java        ← AI classification (3 providers)
│   ├── controller/
│   │   └── RuleEngineController.java       ← REST endpoints
│   ├── model/
│   │   ├── EmailPayload.java               ← Zapier input DTO
│   │   ├── RuleEngineResponse.java         ← Response DTO
│   │   ├── EmailRecord.java                ← JPA entity (H2)
│   │   ├── EmailRepository.java            ← JPA repository
│   │   ├── ActionTracker.java              ← Tracks fired rules/actions
│   │   └── AIClassificationResult.java     ← AI result model
│   ├── rules/
│   │   ├── EmailRule.java                  ← Rule interface
│   │   ├── InvoiceRule.java                ← Priority 1
│   │   ├── SupportRule.java                ← Priority 2
│   │   └── OtherRules.java                 ← Attachment, Spam, Urgent, Newsletter, Fallback
│   ├── service/
│   │   └── RuleEngineService.java          ← Main orchestrator
│   └── config/
│       └── SecurityConfig.java             ← Spring Security config
├── src/main/resources/
│   └── application.properties             ← Config (AI provider, H2, ports)
├── GmailRuleEngine.postman_collection.json ← Import into Postman
└── pom.xml
```

---

## Quick Start

### Prerequisites
- Java 25
- Apache Maven 3.9.6

### 1. Run the server
```bash
cd gmail-rule-engine
mvn spring-boot:run
```
Server starts at: **http://localhost:8080**

### 2. Import Postman collection
- Open Postman → Import → select `GmailRuleEngine.postman_collection.json`
- All requests are pre-configured and ready to send

### 3. Test immediately (no setup needed)
```
POST http://localhost:8080/api/rules/test?type=invoice
POST http://localhost:8080/api/rules/test?type=support
POST http://localhost:8080/api/rules/test?type=spam
POST http://localhost:8080/api/rules/test?type=attachment
POST http://localhost:8080/api/rules/test?type=urgent
POST http://localhost:8080/api/rules/test?type=newsletter
```

---

## AI Provider Setup (Free Options)

### Option A: Keyword Fallback (DEFAULT — zero setup)
Already active. No configuration needed.
```properties
ai.provider=keyword
```

---

### Option B: Ollama — Local LLM (100% free, private, offline)

**Best for:** privacy, no API limits, works offline

1. Install Ollama: https://ollama.ai
2. Pull a model:
   ```bash
   ollama pull llama3        # 4.7GB — best quality
   ollama pull mistral       # 4.1GB — fast
   ollama pull gemma:2b      # 1.7GB — lightest
   ```
3. Start Ollama (runs on port 11434 by default):
   ```bash
   ollama serve
   ```
4. Update `application.properties`:
   ```properties
   ai.provider=ollama
   ai.ollama.model=llama3
   ai.ollama.url=http://localhost:11434
   ```

---

### Option C: Hugging Face Inference API (free tier)

**Best for:** no local setup, cloud-based, 30,000 free requests/month

1. Sign up at https://huggingface.co (free)
2. Get your token: https://huggingface.co/settings/tokens
3. Update `application.properties`:
   ```properties
   ai.provider=huggingface
   ai.huggingface.token=hf_YOUR_TOKEN_HERE
   ai.huggingface.model=facebook/bart-large-mnli
   ```

The model `facebook/bart-large-mnli` does zero-shot classification —
no fine-tuning needed, works out of the box.

---

## REST API Reference

### POST /api/rules/evaluate
Main webhook endpoint. Zapier posts here when a new Gmail arrives.

**Request:**
```json
{
  "from":        "billing@acmecorp.com",
  "to":          "yourname@gmail.com",
  "subject":     "Invoice #1042 from Acme Corp",
  "body_plain":  "Please find your invoice for March...",
  "body_html":   "<p>Please find your invoice...</p>",
  "date":        "Thu, 13 Mar 2026 09:23:14 +0000",
  "message_id":  "<CABcd123@mail.gmail.com>",
  "attachments": ["https://s3.amazonaws.com/invoice.pdf"]
}
```

**Response:**
```json
{
  "messageId":       "<CABcd123@mail.gmail.com>",
  "matchedRules":    ["InvoiceRule", "AttachmentRule"],
  "actionsExecuted": ["SAVE_DB", "NOTIFY_SLACK", "UPLOAD_S3"],
  "status":          "processed",
  "aiCategory":      "INVOICE",
  "aiConfidence":    0.85,
  "aiModel":         "keyword-fallback",
  "processedAt":     "2026-03-13T09:23:15Z"
}
```

---

### POST /api/rules/test?type={type}
Quick test with pre-built sample emails. No body required.

| type        | What it simulates                          |
|-------------|-------------------------------------------|
| invoice     | Billing email with PDF attachment         |
| support     | Customer help request                     |
| spam        | Promotional email with unsubscribe        |
| attachment  | Email with multiple file attachments      |
| urgent      | Critical production alert                 |
| newsletter  | Weekly digest email                       |

---

### GET /api/rules/emails
List all processed emails stored in H2.

Query params:
- `?category=INVOICE` — filter by AI category
- `?status=processed` — filter by status

---

### GET /api/rules/emails/{id}
Get a single processed email by database ID.

---

### GET /api/rules/status
Returns server health and configuration summary.

---

## H2 Database Console

View all stored emails in a browser:
```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:ruleenginedb
User:     sa
Password: (leave blank)
```

Useful SQL queries:
```sql
-- All processed emails
SELECT * FROM PROCESSED_EMAILS;

-- Emails by AI category
SELECT * FROM PROCESSED_EMAILS WHERE AI_CATEGORY = 'INVOICE';

-- Summary by category
SELECT AI_CATEGORY, COUNT(*) as total
FROM PROCESSED_EMAILS
GROUP BY AI_CATEGORY;
```

---

## Rule Engine — How Rules Work

Rules are evaluated in **priority order**. Multiple rules can match the same email.

| Priority | Rule           | Condition                                    | Actions                        |
|----------|----------------|----------------------------------------------|-------------------------------|
| 1        | InvoiceRule    | AI=INVOICE OR subject contains "invoice"     | SAVE_DB, NOTIFY_SLACK         |
| 2        | SupportRule    | AI=SUPPORT OR from/subject contains "help"   | CREATE_TICKET, SEND_ACK       |
| 3        | AttachmentRule | AI=ATTACHMENT OR has attachments             | UPLOAD_S3, LOG                |
| 4        | SpamRule       | AI=SPAM OR body contains "unsubscribe"       | MARK_SPAM, SKIP               |
| 5        | UrgentRule     | AI=URGENT OR subject contains "urgent"       | SEND_SMS_ALERT, NOTIFY_MANAGER|
| 6        | NewsletterRule | AI=NEWSLETTER OR subject contains "weekly"   | ARCHIVE, LABEL_NEWSLETTER     |
| 99       | FallbackRule   | Always (if no other rule matched)            | LOG_ONLY                      |

### Adding a New Rule
1. Create a new class implementing `EmailRule`
2. Annotate with `@Component`
3. Set a unique `priority()`
4. Implement `evaluate()` and `execute()`

Spring auto-discovers and registers it — no other changes needed.

---

## Connect to Zapier

1. In Zapier: **New Zap → Trigger: Gmail → New Email**
2. **Action: Webhooks by Zapier → POST**
3. URL: `https://your-server.com/api/rules/evaluate`
4. Payload Type: JSON
5. Map fields:
   ```
   from        → {{From Email}}
   to          → {{To Email}}
   subject     → {{Subject}}
   body_plain  → {{Body Plain}}
   body_html   → {{Body HTML}}
   date        → {{Date}}
   message_id  → {{Message ID}}
   attachments → {{Attachment}}
   ```
6. Headers: `X-Zapier-Secret: your-secret`
7. Set env var on your server: `ZAPIER_SECRET=your-secret`

---

## Deploy to Cloud (Free Options)

### Railway.app (free tier)
```bash
# Install Railway CLI
npm install -g @railway/cli
railway login
railway init
railway up
```

### Render.com (free tier)
- Connect GitHub repo → New Web Service → Java → `mvn spring-boot:run`

### Google Cloud Run (free tier — 2M requests/month)
```bash
mvn package
gcloud run deploy rule-engine --source .
```
