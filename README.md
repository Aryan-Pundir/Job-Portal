# Job-Portal
Job Portal REST API built with Spring Boot Microservices, MongoDB, Apache Kafka, JWT, Spring Security, and Feign Client
## 🚀 Job Portal - Microservices Architecture

A production-ready Job Portal backend built with Spring Boot Microservices.

### 🏗️ Architecture
- **API Gateway** - Single entry point, JWT validation, request routing
- **Auth Service** - User registration & login with JWT
- **Job Service** - Job posting & management with Kafka producer
- **Application Service** - Job applications with Feign Client + Kafka producer
- **Notification Service** - Email alerts via Kafka consumer

### ⚙️ Tech Stack
`Java 17` `Spring Boot 3` `MongoDB` `Apache Kafka` `Spring Security` `JWT` `Feign Client` `Spring Cloud Gateway`

### ✨ Key Features
- Role-based access control (Employer / Job Seeker)
- Async email notifications via Kafka
- Inter-service communication via Feign Client
- Separate MongoDB database per service
- JWT authentication at API Gateway level

### 📡 API Endpoints
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/jobs/all` | Public |
| POST | `/api/jobs/post` | Employer |
| POST | `/api/applications/apply/{jobId}` | Job Seeker |
