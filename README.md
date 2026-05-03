# GovBudget — Kamu Bütçe Yönetim Sistemi

> Enterprise-grade public budget management platform built for government institutions.  
> Production-style Java 17 + Spring Boot 3 backend with full security, event streaming, and audit trail.

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![CI](https://github.com/talhayilmazc/GovBudget-Public-Finance-Management-System/actions/workflows/ci.yml/badge.svg)

---

## 🏛️ What This System Does

GovBudget is a production-style budget and expenditure management platform designed for government institutions.  
It models how real public finance systems handle budget allocation, expense approval workflows, audit trails, and real-time event streaming.

This is **not a CRUD demo** — it implements:

- ✅ Role-based access control (RBAC) with 5 distinct roles
- ✅ Full budget lifecycle (Draft → Pending → Approved → Active → Closed)
- ✅ Maker-checker expense approval workflow
- ✅ Immutable audit trail for every action
- ✅ Real-time event streaming via Apache Kafka
- ✅ Redis caching for high-performance reads
- ✅ JWT-based stateless authentication
- ✅ OpenAPI/Swagger documentation
- ✅ GitHub Actions CI/CD pipeline
- ✅ Docker + Kubernetes ready

---

## 🏗️ Architecture
Client
│
▼
Spring Security (JWT Filter)
│
▼
REST Controllers  ──►  Service Layer  ──►  Repository Layer  ──►  PostgreSQL
│
▼
Kafka Event Producer  ──►  Kafka Topics
│
▼
Redis Cache Layer

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3, Spring MVC, Spring Security, Spring Data JPA |
| Database | PostgreSQL 15 + Hibernate/JPA ORM |
| Caching | Redis 7 |
| Messaging | Apache Kafka 7.5 |
| Auth | JWT (jjwt 0.11.5) |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Build | Maven 3.9 |
| DevOps | Docker, Docker Compose, GitHub Actions CI/CD |
| Testing | JUnit 5, Mockito, AssertJ |

---

## 🔐 RBAC — Role Based Access Control

| Role | Capabilities |
|---|---|
| `ROLE_ADMIN` | Full system access |
| `ROLE_BUDGET_MANAGER` | Create, approve, reject budgets and expenses |
| `ROLE_FINANCE_OFFICER` | Submit expenses |
| `ROLE_AUDITOR` | View all data and audit logs |
| `ROLE_VIEWER` | Read-only access |

---

## 📋 Budget Lifecycle
DRAFT ──► PENDING ──► APPROVED ──► ACTIVE ──► CLOSED
└──► REJECTED
└──► CANCELLED

---

## 📁 Project Structure
src/main/java/com/hazine/govbudget/
├── config/          # Security, JPA, Redis, Kafka, OpenAPI configs
├── controller/      # REST API endpoints
├── domain/
│   ├── entity/      # JPA entities (User, Department, Budget, Expense, AuditLog)
│   ├── enums/       # Role, BudgetStatus, ExpenseStatus, ExpenseCategory, AuditAction
│   └── repository/  # Spring Data JPA repositories
├── dto/
│   ├── request/     # Validated request DTOs
│   └── response/    # Response DTOs
├── event/           # Kafka producers, consumers and event models
├── exception/       # Global exception handling
├── security/        # JWT provider, filter, UserPrincipal
└── service/         # Business logic (interfaces + implementations)

---

## 🚀 Running Locally

### Prerequisites
- Docker Desktop
- Java 17
- Maven 3.9+

### Start all services

```bash
docker compose up -d
```

This starts:
- **App** → http://localhost:8080
- **PostgreSQL** → localhost:5432
- **Redis** → localhost:6379
- **Kafka** → localhost:9092
- **Kafka UI** → http://localhost:8090

### API Documentation
http://localhost:8080/swagger-ui/index.html

### Health Check
http://localhost:8080/actuator/health

---

## 🔑 Authentication

### Register

```bash
POST /api/v1/auth/register
{
  "username": "talha",
  "password": "password123",
  "email": "talha@hazine.gov.tr",
  "firstName": "Talha",
  "lastName": "Yılmaz",
  "departmentId": 1
}
```

### Login

```bash
POST /api/v1/auth/login
{
  "username": "talha",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "talha",
  "fullName": "Talha Yılmaz"
}
```

---

## 📊 Key API Endpoints

### Budgets
| Method | Endpoint | Description | Role |
|---|---|---|---|
| POST | `/api/v1/budgets` | Create budget | ADMIN, BUDGET_MANAGER |
| GET | `/api/v1/budgets` | List all (paginated) | All |
| GET | `/api/v1/budgets/{id}` | Get by ID | All |
| PATCH | `/api/v1/budgets/{id}/approve` | Approve budget | ADMIN, BUDGET_MANAGER |
| PATCH | `/api/v1/budgets/{id}/reject` | Reject budget | ADMIN, BUDGET_MANAGER |
| PATCH | `/api/v1/budgets/{id}/activate` | Activate budget | ADMIN, BUDGET_MANAGER |
| GET | `/api/v1/budgets/fiscal-year/{year}` | Filter by fiscal year | All |

### Expenses
| Method | Endpoint | Description | Role |
|---|---|---|---|
| POST | `/api/v1/expenses` | Submit expense | FINANCE_OFFICER+ |
| PATCH | `/api/v1/expenses/{id}/approve` | Approve expense | BUDGET_MANAGER+ |
| PATCH | `/api/v1/expenses/{id}/reject` | Reject expense | BUDGET_MANAGER+ |
| GET | `/api/v1/expenses/budget/{id}` | Expenses by budget | All |

### Audit
| Method | Endpoint | Description | Role |
|---|---|---|---|
| GET | `/api/v1/audit-logs/entity/{name}/{id}` | Entity audit trail | ADMIN, AUDITOR |
| GET | `/api/v1/audit-logs/user/{username}` | User activity | ADMIN, AUDITOR |

---

## 🧪 Testing

```bash
mvn test
```

Test coverage includes:
- ✅ BudgetService — 6 unit tests
- ✅ ExpenseService — 6 unit tests  
- ✅ DepartmentService — 4 unit tests
- ✅ Business rule validation
- ✅ Exception handling
- ✅ Mockito-based isolation

---

## 📡 Kafka Event Streaming

| Topic | Event | Trigger |
|---|---|---|
| `budget-events` | BudgetEvent | Create, approve, reject, activate, close |
| `expense-events` | ExpenseEvent | Create, approve, reject |
| `audit-events` | AuditEvent | All system actions |

Monitor events live via **Kafka UI**: http://localhost:8090

---

## 🔄 CI/CD Pipeline

GitHub Actions pipeline runs on every push:

1. **Build** — `mvn clean compile`
2. **Test** — `mvn test` with PostgreSQL + Redis services
3. **Docker Build** — builds image on `main` and `develop` branches

---

## 👨‍💻 Author

**Talha Yılmaz**  
[github.com/talhayilmazc](https://github.com/talhayilmazc) · [linkedin.com/in/talha-yilmaz-38a13a225](https://linkedin.com/in/talha-yilmaz-38a13a225)