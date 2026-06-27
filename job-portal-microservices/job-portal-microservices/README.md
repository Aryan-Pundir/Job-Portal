# Job Portal - Microservices Architecture

**Stack:** Spring Boot 3 | MongoDB | Apache Kafka | Spring Security | JWT | Feign Client | API Gateway

---

## Architecture Overview

```
                        ┌─────────────────┐
                        │   API GATEWAY   │  :8080
                        │  (JWT Validate) │
                        └────────┬────────┘
                                 │ Routes requests
              ┌──────────────────┼──────────────────┐
              │                  │                   │
     ┌────────▼──────┐  ┌───────▼───────┐  ┌───────▼──────────┐
     │ AUTH SERVICE  │  │  JOB SERVICE  │  │ APPLICATION SVC  │
     │    :8081      │  │    :8082      │  │     :8083        │
     │   MongoDB     │  │   MongoDB     │  │    MongoDB       │
     │   (authdb)    │  │   (jobdb)     │  │  (applicationdb) │
     └───────────────┘  └───────┬───────┘  └────────┬─────────┘
                                │ Kafka              │ Kafka
                                │ Produce            │ Produce
                         ┌──────▼────────────────────▼──────┐
                         │         APACHE KAFKA              │
                         │   Topics:                         │
                         │   - job-posted                    │
                         │   - application-received          │
                         └──────────────────┬────────────────┘
                                            │ Consume
                                   ┌────────▼────────┐
                                   │ NOTIFICATION SVC │
                                   │     :8084        │
                                   │  Sends Emails    │
                                   └─────────────────┘
```

---

## Services & Ports

| Service | Port | DB | Role |
|---------|------|----|------|
| API Gateway | 8080 | - | Routes + JWT validation |
| Auth Service | 8081 | authdb | Register, Login |
| Job Service | 8082 | jobdb | Post/manage jobs + Kafka producer |
| Application Service | 8083 | applicationdb | Apply for jobs + Feign + Kafka producer |
| Notification Service | 8084 | - | Kafka consumer + email alerts |

---

## How to Run

### Step 1 — Start MongoDB
```bash
mongod
```

### Step 2 — Start Kafka + Zookeeper
```bash
# Start Zookeeper
zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (new terminal)
kafka-server-start.sh config/server.properties
```

### Step 3 — Run all 5 services in IntelliJ
Open each service folder as a separate module and run:
1. `ApiGatewayApplication.java`
2. `AuthServiceApplication.java`
3. `JobServiceApplication.java`
4. `ApplicationServiceApplication.java`
5. `NotificationServiceApplication.java`

---

## API Endpoints (all via Gateway on port 8080)

### Auth
```
POST  /api/auth/register   → Register (ROLE_JOBSEEKER or ROLE_EMPLOYER)
POST  /api/auth/login      → Get JWT token
```

### Jobs (public)
```
GET   /api/jobs/all                                      → All open jobs
GET   /api/jobs/{id}                                     → Job by ID
GET   /api/jobs/search?title=java&location=bangalore     → Search
```

### Jobs (Employer only)
```
POST  /api/jobs/post        → Post job (triggers Kafka: job-posted)
PUT   /api/jobs/{id}        → Update job
DELETE /api/jobs/{id}       → Delete job
PUT   /api/jobs/{id}/close  → Close job
GET   /api/jobs/my-jobs     → My posted jobs
```

### Applications
```
POST   /api/applications/apply/{jobId}       → Apply (JOBSEEKER) — triggers Kafka
GET    /api/applications/my-applications     → My applications (JOBSEEKER)
DELETE /api/applications/{id}/withdraw       → Withdraw (JOBSEEKER)
GET    /api/applications/job/{jobId}         → All applicants (EMPLOYER)
PUT    /api/applications/{id}/status?status= → Update status (EMPLOYER)
```

Application statuses: `PENDING → REVIEWED → SHORTLISTED → HIRED / REJECTED`

---

## Kafka Flow

### Flow 1: Job Posted
```
Employer posts job
  → Job Service saves to MongoDB
  → Job Service publishes to topic: job-posted
  → Notification Service consumes event
  → Sends confirmation email to employer
```

### Flow 2: Application Submitted
```
Jobseeker applies
  → Application Service calls Job Service via Feign Client (validates job exists)
  → Application Service saves to MongoDB
  → Application Service publishes to topic: application-received
  → Notification Service consumes event
  → Sends email to applicant (confirmation)
  → Sends email to employer (new application alert)
```

---

## Key Concepts (Interview Points)

| Concept | Where Used |
|---------|------------|
| **API Gateway** | Single entry point, JWT validation, routing |
| **Feign Client** | Application Service → Job Service (sync HTTP) |
| **Kafka Producer** | Job Service & Application Service publish events |
| **Kafka Consumer** | Notification Service consumes events |
| **JWT Auth** | Validated at Gateway + each service independently |
| **Role-based Access** | `@PreAuthorize` in Job & Application controllers |
| **MongoDB** | Separate DB per service (database-per-service pattern) |
| **Async Communication** | Kafka decouples notification from business logic |
