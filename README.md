# Smart Water Usage Monitoring and Automated Billing Management Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18.3-61dafb.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An enterprise full-stack web platform built for urban apartment communities to monitor household water consumption, eliminate unaccounted usage, distribute shared bulk water tanker procurement costs fairly, and automate progressive tiered billing with itemized PDF invoices.

---

## Table of Contents
- [1. Overview & Problem Statement](#1-overview--problem-statement)
- [2. Key Features & Capabilities](#2-key-features--capabilities)
- [3. Technology Stack](#3-technology-stack)
- [4. Architecture & Repository Structure](#4-architecture--repository-structure)
- [5. Prerequisites & Environment Setup](#5-prerequisites--environment-setup)
- [6. Database Setup & Migrations](#6-database-setup--migrations)
- [7. Configuration & Environment Variables](#7-configuration--environment-variables)
- [8. Building & Running the Application](#8-building--running-the-application)
- [9. Swagger & OpenAPI Documentation](#9-swagger--openapi-documentation)
- [10. Authentication & Role Separation Workflows](#10-authentication--role-separation-workflows)
- [11. Core Domain Workflows](#11-core-domain-workflows)
  - [11.1 Submeter Provisioning & Reading Ingestion](#111-submeter-provisioning--reading-ingestion)
  - [11.2 Progressive Tiered Tariff Engine](#112-progressive-tiered-tariff-engine)
  - [11.3 Bulk Tanker Procurement & Cost Allocation](#113-bulk-tanker-procurement--cost-allocation)
  - [11.4 Billing Cycle Lifecycle & PDF Invoicing](#114-billing-cycle-lifecycle--pdf-invoicing)
  - [11.5 Statistical Anomaly & Leak Detection](#115-statistical-anomaly--leak-detection)
  - [11.6 Resident Portal & Peer Benchmarking](#116-resident-portal--peer-benchmarking)
- [12. Testing & Verification](#12-testing--verification)
- [13. Troubleshooting](#13-troubleshooting)
- [14. Screenshots & Visual Interface](#14-screenshots--visual-interface)
- [15. Limitations & Future Roadmap](#15-limitations--future-roadmap)
- [16. Project Attribution & License](#16-project-attribution--license)

---

## 1. Overview & Problem Statement

### The Problem
In multi-tenant apartment complexes and residential societies, water management faces significant friction:
1. **Unaccounted Consumption:** Flats without submeter tracking consume water without accountability.
2. **Unfair Flat-Rate Billing:** When water tankers are procured during municipal shortages, costs are divided equally among all flats. Low-consumption and single-occupant households unfairly subsidize high-consumption households.
3. **Undetected Leaks:** Plumbing leaks often go unnoticed until physical seepage occurs or monthly tanker expenses soar.
4. **Manual Overhead:** Resident welfare associations (RWAs) manually record meter numbers, calculate tiered tariffs on spreadsheets, and distribute paper receipts.

### The Solution
This platform provides a complete **software-only** solution that:
- Captures digital/mechanical submeter readings via manual entry or bulk CSV uploads.
- Calculates consumption using a **progressive tiered tariff engine** (higher brackets pay higher per-kL rates).
- Dynamically allocates shared bulk tanker procurement expenses proportional to each household's measured consumption.
- Evaluates rolling 30-day usage baselines using statistical anomaly detection ($\mu + 2\sigma$) to flag continuous leaks.
- Issues itemized bills with cryptographic state enforcement and generates official downloadable iText PDF invoices.

---

## 2. Key Features & Capabilities

- **Role-Based Authentication & Authorization:**
  - Dedicated **Owner/Admin** and **Resident** login and registration portals.
  - Dynamic **first-owner bootstrap**: initial administrator is created securely when no admin exists; subsequent public admin registrations are permanently locked out.
  - Server-enforced role separation: Owner Login rejects Resident accounts; Resident Login rejects Admin accounts without issuing JWTs.
  - Passwords hashed with salted **BCrypt**; stateless **JWT (HMAC-SHA256)** session tokens.
- **Apartment & Household Submetering:**
  - Multi-apartment, multi-block, and flat management.
  - Submeter binding with mechanical and digital meter types.
- **Reading Ingestion & Validation:**
  - Single meter reading submission with non-decreasing continuity validation ($R_{current} \ge R_{previous}$).
  - Multi-meter CSV batch upload with transactional parsing, row error logs, and downloadable standard template.
- **Progressive Tiered Tariffs:**
  - Multi-bracket tariff plans (e.g., Tier 1: 0–10 kL @ \$20/kL; Tier 2: 10–20 kL @ \$30/kL; Tier 3: >20 kL @ \$45/kL).
- **Bulk Tanker Procurement & Fair Shared Allocation:**
  - Recording municipal and private vendor tanker deliveries, volumes, and invoices.
  - Proportional cost allocation algorithm distributing tanker expenses based on individual consumption ratios with square-footage area fallback.
- **Billing Lifecycle State Machine:**
  - One-way state machine: `OPEN` $\rightarrow$ `CALCULATING` $\rightarrow$ `FINALIZED` $\rightarrow$ `ARCHIVED`.
  - Immutable billing guarantees once finalized.
  - Itemized downloadable **iText PDF Invoices** for every flat.
- **Statistical Leak Detection:**
  - Gaussian anomaly detection algorithm comparing daily consumption against a 30-day baseline mean plus two standard deviations ($\mu + 2\sigma$).
  - Anomaly status lifecycle (`DETECTED` $\rightarrow$ `INVESTIGATING` $\rightarrow$ `RESOLVED`).
- **Resident Experience & Peer Benchmarking:**
  - Interactive Recharts usage trends (daily/monthly).
  - Anonymized building consumption percentiles and water-conservation scores.
  - In-app notification center for bill publishing and anomaly warnings.

---

## 3. Technology Stack

### Backend
- **Language & Runtime:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.4
- **Security:** Spring Security 6, JJWT (io.jsonwebtoken 0.12.5), BCrypt
- **Persistence:** Spring Data JPA, Hibernate 6
- **Database:** PostgreSQL 17
- **Database Migrations:** Flyway 10
- **Document Generation:** iText 7 (com.itextpdf 7.2.5)
- **API Documentation:** SpringDoc OpenAPI 2.6.0 (Swagger UI)
- **Build Tool:** Apache Maven 3.9+

### Frontend
- **Framework:** React 18.3
- **Build Tool:** Vite 5.4
- **Routing:** React Router DOM 6.22
- **State & Networking:** React Context API, Axios 1.6
- **Visualizations & Charts:** Recharts 2.12
- **Styling:** Modular CSS, responsive grid layouts (blue/white design system)

---

## 4. Architecture & Repository Structure

```
smart-water-usage-platform/
├── backend/
│   ├── pom.xml                               # Maven project dependencies & build configuration
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartwater/
│   │   │   │   ├── config/                   # SecurityConfig, CorsConfig
│   │   │   │   ├── controller/               # Auth, Apartment, Household, Meter, Usage, Tariff, Bill, Anomaly
│   │   │   │   ├── dto/                      # Strongly-typed request/response transfer objects
│   │   │   │   ├── entity/                   # JPA database entities (User, Apartment, Household, Bill, etc.)
│   │   │   │   ├── exception/                # GlobalExceptionHandler & custom exception classes
│   │   │   │   ├── repository/               # Spring Data JPA repositories
│   │   │   │   ├── security/                 # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│   │   │   │   └── service/                  # Core domain logic & calculation engines
│   │   │   └── resources/
│   │   │       ├── application.yml           # Spring Boot configuration (profile defaults)
│   │   │       ├── application-demo.yml      # Standalone demonstration profile
│   │   │       ├── application-h2.yml        # In-memory testing profile
│   │   │       └── db/migration/             # Flyway SQL migrations (V1 initial, V2 complete platform)
│   │   └── test/java/com/smartwater/         # 37 automated JUnit 5 / Mockito unit & integration tests
├── frontend/
│   ├── package.json                          # Frontend dependencies & npm scripts
│   ├── vite.config.js                        # Vite bundler configuration & local proxy
│   ├── .env.example                          # Frontend environment variable template
│   ├── index.html                            # Application single-page HTML entry point
│   └── src/
│       ├── components/                       # Shared UI components (Navbar, PublicNavbar, ProtectedRoute)
│       ├── context/                          # AuthContext (JWT session restoration, login/logout)
│       ├── layouts/                          # AdminLayout, PublicLayout
│       ├── pages/                            # 20 application views (Portals, Dashboards, CRUD management)
│       ├── services/                         # Axios client instance with Bearer token interceptor
│       └── utils/                            # Date formatters, math utilities, previewMode helper
├── docs/                                     # Architecture, security reviews, test reports, and Postman suites
│   ├── DEPLOYMENT_AND_DEMO_GUIDE.md          # Comprehensive deployment and walkthrough guide
│   ├── FINAL_AUDIT_REPORT.md                 # Complete system audit report
│   ├── IMPLEMENTATION_STATUS.md              # Feature delivery matrix and verification status
│   ├── SECURITY_REVIEW.md                    # Threat analysis, BOLA/IDOR mitigations, and compliance
│   ├── TEST_RESULTS.md                       # Test metrics and build logs
│   └── postman/                              # Postman collection & environment for API testing
├── .env.example                              # Root environment variable template
├── .gitignore                                # Comprehensive Git exclusions
├── CHANGELOG.md                              # Chronological project releases and updates
└── README.md                                 # Primary repository documentation
```

---

## 5. Prerequisites & Environment Setup

Before building and running the platform, ensure the following software is installed on your workstation:

| Prerequisite | Minimum Version | Verification Command |
| :--- | :--- | :--- |
| **Java JDK** | 21 (LTS) | `java -version` |
| **Apache Maven** | 3.9+ | `mvn -version` |
| **Node.js** | 18+ (tested on v20/v22) | `node -v` |
| **npm** | 9+ | `npm -v` |
| **PostgreSQL** | 15+ (tested on v17) | `psql -U postgres -c "SELECT version();"` |

---

## 6. Database Setup & Migrations

1. Start your local PostgreSQL server:
   ```bash
   # Linux/macOS
   sudo systemctl start postgresql

   # Windows
   net start postgresql-x64-17
   ```

2. Connect to PostgreSQL and create the application database:
   ```sql
   CREATE DATABASE smart_water_db;
   ```

3. **Flyway Automatic Migrations:**
   You do **not** need to manually execute SQL scripts. When Spring Boot starts, Flyway automatically scans `backend/src/main/resources/db/migration/` and applies:
   - `V1__init_schema.sql`: Initial core tables (`users`, `apartments`, `households`, `meters`, `water_usage_logs`).
   - `V2__complete_platform_schema.sql`: Tariff tiers, bulk purchases, shared allocations, billing cycles, bills, bill items, anomaly events, and notifications.

---

## 7. Configuration & Environment Variables

Copy the provided `.env.example` to configure your environment:

```bash
cp .env.example .env
```

### Backend Environment Variables
| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| `DB_URL` | JDBC database connection string | `jdbc:postgresql://localhost:5432/smart_water_db` |
| `DB_USERNAME` | PostgreSQL database user | `postgres` |
| `DB_PASSWORD` | PostgreSQL database password | `postgres` |
| `JWT_SECRET` | 256-bit hexadecimal key for signing tokens | `404E6352...566B5970` |
| `JWT_EXPIRATION_MS`| JWT session validity duration in ms | `86400000` (24 hours) |
| `SERVER_PORT` | HTTP server port | `8080` |

### Frontend Environment Variables
In `frontend/.env.example`:
| Variable | Description | Default |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | Base URL for Spring Boot REST endpoints | `http://localhost:8080/api` |
| `VITE_ENABLE_PREVIEW_MODE`| Development preview mode toggle | `false` |

---

## 8. Building & Running the Application

### 8.1 Backend (Spring Boot)

```bash
cd backend

# Compile and run automated test suite
mvn clean test

# Package into executable JAR
mvn clean package -DskipTests

# Run Spring Boot service
mvn spring-boot:run
```
The backend initializes on `http://localhost:8080`.

### 8.2 Frontend (React + Vite)

```bash
cd frontend

# Install dependencies
npm install

# Build production distribution
npm run build

# Start local development server
npm run dev
```
The frontend UI opens on `http://localhost:5173`.

---

## 9. Swagger & OpenAPI Documentation

Once the backend is running, explore and interact with all endpoints via Swagger UI:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

To execute authenticated requests in Swagger, click the **Authorize** button at the top right and enter:
```text
Bearer <your_jwt_token>
```

---

## 10. Authentication & Role Separation Workflows

```
       ┌──────────────────────────────────────────────────────────┐
       │                   Public Website (/)                     │
       └────────────────────────────┬─────────────────────────────┘
                                    │
              ┌─────────────────────┴─────────────────────┐
              ▼                                           ▼
   ┌──────────────────────┐                   ┌──────────────────────┐
   │ Resident Portal      │                   │ Apartment Owner /    │
   │ /resident/login      │                   │ Admin Console        │
   │ /resident/register   │                   │ /admin/login         │
   └──────────┬───────────┘                   │ /admin/register      │
              │                               └──────────┬───────────┘
              │ [ role == RESIDENT ]                     │ [ role == ADMIN ]
              ▼                                          ▼
   ┌──────────────────────┐                   ┌──────────────────────┐
   │ Resident Dashboard   │                   │ Admin Dashboard      │
   │ /resident/dashboard  │                   │ /admin/dashboard     │
   └──────────────────────┘                   └──────────────────────┘
```

### Initial Owner Bootstrap Process
1. On a pristine database, navigate to `http://localhost:5173/admin/register` (or `POST /api/auth/register/owner`).
2. Provide Owner Name, Email, and Password. **No administrative secret key is required.**
3. The server checks `!userRepository.existsByRole(Role.ADMIN)`:
   - If true, creates the initial account with `ROLE_ADMIN`.
   - If false, rejects the request with HTTP 403 (*"An administrator already exists. Public registration of additional administrator accounts is disabled."*).
4. After successful registration, the user is redirected to **Owner Login** (`/admin/login`).

### Resident Registration Process
1. Navigate to `http://localhost:5173/resident/register` (or `POST /api/auth/register/resident`).
2. Provide Full Name, Email, and Password.
3. The backend **strictly enforces `Role.RESIDENT`**, completely ignoring any role submitted in the payload.
4. On success, the UI displays a confirmation screen with a button to **Resident Login** (`/resident/login`).

### Strict Login Separation & Denial
- **Owner Login (`/admin/login`):** Validates credentials against `POST /api/auth/login/owner`. If an account with `role: RESIDENT` attempts to sign in, the server returns HTTP 401:
  > *"Access denied: This account is registered as a Resident. Please sign in via the Resident Portal."*
- **Resident Login (`/resident/login`):** Validates credentials against `POST /api/auth/login/resident`. If an account with `role: ADMIN` attempts to sign in, the server returns HTTP 401:
  > *"Access denied: This portal is for Residents only. Apartment Owners/Administrators must use the Owner Login page."*

---

## 11. Core Domain Workflows

### 11.1 Submeter Provisioning & Reading Ingestion
- Administrators register apartments (`/admin/apartments`), configure household units (`/admin/households`), and assign physical or digital meters (`/admin/meters`).
- Readings are recorded via:
  - **Manual Entry (`/admin/usage`):** Form-based entry specifying meter, date, and current reading.
  - **Bulk CSV Upload (`/admin/usage`):** Upload standard comma-separated files containing `meter_number,reading_date,current_reading`.

### 11.2 Progressive Tiered Tariff Engine
- Tiered tariff structures encourage conservation by progressively charging higher rates for higher consumption brackets.
- Example structure:
  $$\text{Total Bill} = (V_{\text{Tier 1}} \times R_1) + (V_{\text{Tier 2}} \times R_2) + (V_{\text{Tier 3}} \times R_3)$$
- For a household consuming 18 kL under a plan with Tier 1 (0–10 kL @ \$20) and Tier 2 (>10 kL @ \$30):
  $$\text{Tariff Charge} = (10 \times 20) + (8 \times 30) = 200 + 240 = \$440.00$$

### 11.3 Bulk Tanker Procurement & Cost Allocation
- When municipal water is insufficient, community tankers are recorded under `/admin/purchases` (vendor, date, volume in kL, and invoice amount).
- During cycle calculation, common tanker procurement expenses are shared fairly:
  $$\text{Household Share} = \text{Total Tanker Cost} \times \left( \frac{\text{Household Metered Consumption}}{\text{Total Apartment Metered Consumption}} \right)$$
- If a unit has zero metered readings, a fallback calculation based on flat area square footage is applied.

### 11.4 Billing Cycle Lifecycle & PDF Invoicing
- Administrators create monthly billing cycles (`/admin/billing`).
- Cycle transitions through the strict lifecycle:
  `OPEN` (reading collection) $\rightarrow$ `CALCULATING` (tariff & allocation math) $\rightarrow$ `FINALIZED` (immutable billing).
- Upon finalization, residents can view their itemized statement under `/resident/bills` and download an official **iText PDF invoice** containing:
  - Meter reading delta ($R_{current} - R_{previous} = \text{Consumption}$).
  - Tiered tariff rate breakdown.
  - Shared bulk tanker cost line item.
  - Total amount due and payment terms.

### 11.5 Statistical Anomaly & Leak Detection
- Under `/admin/alerts`, the anomaly detection engine scans trailing 30-day consumption logs for each household.
- If a daily reading exceeds the baseline threshold:
  $$\text{Threshold} = \mu + 2\sigma \quad (\text{Mean} + 2 \times \text{Standard Deviation})$$
  an `AnomalyEvent` is automatically generated with status `DETECTED`.
- Admins can investigate the incident, record physical inspection notes, and mark it `CONFIRMED_LEAK` or `RESOLVED`.
- Affected residents receive an immediate notification in their portal.

### 11.6 Resident Portal & Peer Benchmarking
- Residents track their household's daily and monthly consumption through responsive **Recharts** visualizations.
- The **Peer Benchmarking** engine compares individual consumption against the community average:
  - Average consumption per occupant.
  - Household percentile rank within the building.
  - Actionable conservation tips based on usage tier.

---

## 12. Testing & Verification

The project includes an extensive automated test suite covering unit operations, data isolation, and financial calculation integrity.

### Run Automated Backend Tests
```bash
cd backend
mvn clean test
```

### Verified Test Results (37/37 Tests Passing)
```text
[INFO] Running com.smartwater.AnomalyDetectionServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.AuthControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.AuthServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.BillingCycleServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.CsvUploadServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.EndToEndWorkflowTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.HouseholdDataIsolationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.SharedCostAllocationServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.TariffPlanServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.smartwater.WaterUsageServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Run Frontend Production Build
```bash
cd frontend
npm run build
```
Output:
```text
✓ 922 modules transformed.
dist/index.html                   0.73 kB │ gzip:   0.41 kB
dist/assets/index-DQkmOisU.css   22.20 kB │ gzip:   4.70 kB
dist/assets/index-BfdmT0T4.js   753.38 kB │ gzip: 205.11 kB
✓ built in 32.10s
```

---

## 13. Troubleshooting

### 1. Database Connection Failure (`Connection to localhost:5432 refused`)
- **Cause:** PostgreSQL service is stopped or port 5432 is unreachable.
- **Solution:** Verify PostgreSQL is running (`pg_isready -h localhost -p 5432`) and that database `smart_water_db` exists (`CREATE DATABASE smart_water_db;`). Verify credentials in `application.yml` or set `DB_PASSWORD`.

### 2. Port Conflict (`Port 8080 or 5173 is already in use`)
- **Backend:** Change port via `server.port` in `application.yml` or pass `SERVER_PORT=8081 mvn spring-boot:run`.
- **Frontend:** Vite will automatically suggest port 5174 if 5173 is occupied, or specify `npm run dev -- --port 3000`.

### 3. Flyway Migration Checksum Mismatch
- **Cause:** Modifying applied SQL files directly in `db/migration/`.
- **Solution:** Never edit applied migrations. For local dev resets, drop and recreate `smart_water_db`.

### 4. JWT Authentication Denial (`401 Unauthorized`)
- **Cause:** Expired token, token tampering, or role mismatch between portal and account.
- **Solution:** Clear browser `localStorage` (`localStorage.clear()`) and sign in through the matching portal (`/admin/login` for Owners, `/resident/login` for Residents).

---

## 14. Screenshots & Visual Interface

| Interface View | Route | Description |
| :--- | :--- | :--- |
| **Public Landing Page** | `/` | Platform value proposition, feature breakdown, and quick portal links |
| **Owner / Admin Login** | `/admin/login` | Secure administrator access with server-side role validation |
| **Resident Login** | `/resident/login` | Resident portal access restricted to household accounts |
| **Admin Dashboard** | `/admin/dashboard` | Community-wide consumption metrics, tariff summaries, active leak alerts |
| **Resident Dashboard** | `/resident/dashboard`| Household consumption trends, billing status, peer benchmarking cards |
| **Tariff & Billing Management** | `/admin/billing` | Tiered tariff configuration, cycle finalization, and PDF generation |

*(Screenshots can be captured during demonstration and archived in `docs/screenshots/`)*

---

## 15. Limitations & Future Roadmap

### Current Limitations
- **Software-Only Model:** Meter readings are ingested via manual logging and batch CSV files. Direct telemetry from proprietary IoT pulse meters requires local gateway integration.
- **Payment Settlement:** Invoices transition between `ISSUED` and `PAID` via administrative recording rather than real-time payment gateway webhooks.
- **Email Delivery:** Resident notifications are managed in-app; automated email dispatch requires an active external SMTP server.

### Future Roadmap
1. **Automated AMR/AMI IoT Gateway:** MQTT/LoRaWAN ingestion adapter for wireless smart water meters.
2. **Payment Gateway Integration:** Direct payment processing through Razorpay/Stripe webhooks for instant invoice settlement.
3. **SMS & WhatsApp Alerts:** Critical leak alerts delivered via Twilio or WhatsApp Business API.
4. **Mobile Application:** Progressive Web App (PWA) or Flutter cross-platform mobile client for residents.

---

## 16. Project Attribution & License

- **Academic Project:** Development of a Smart Water Usage Monitoring and Automated Billing Management Platform.
- **Repository:** [https://github.com/YashwanthGowda81/smart-water-usage-platform](https://github.com/YashwanthGowda81/smart-water-usage-platform)
- **License:** Distributed under the [MIT License](LICENSE).
