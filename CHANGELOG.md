# Changelog

All notable changes to the **Smart Water Usage Monitoring and Automated Billing Management Platform** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.4.0] - 2026-10-14

### Added
- **First-Owner Dynamic Bootstrap**: Server-side initial administrator setup via `userRepository.existsByRole(Role.ADMIN)` without requiring a static admin secret key.
- **Dedicated Role Endpoints**: Added `POST /api/auth/register/resident`, `POST /api/auth/register/owner`, `POST /api/auth/login/resident`, and `POST /api/auth/login/owner`.
- **Server-Side Portal Role Enforcement**: Owner Login rejects Resident accounts with HTTP 401; Resident Login rejects Admin accounts with HTTP 401 without issuing a JWT.
- **Untrusted Input Protection**: Resident registration endpoint forces `Role.RESIDENT` on the backend, ignoring any client-supplied role parameter.
- **Registration Decoupling**: Registration returns a confirmation message without issuing a JWT token, requiring explicit subsequent authentication.
- **Expanded Test Suite**: Added 12 unit tests in `AuthServiceTest` and 5 unit tests in `AuthControllerTest`, bringing the automated backend test suite to 37 passing tests.
- **Environment Templates**: Added root `.env.example` and `frontend/.env.example`.
- **Route Aliases**: Added `/owner/login` and `/owner/register` route aliases in React Router.

### Changed
- **Email Normalization**: Standardized `.trim().toLowerCase()` across user registration, login, lookup, and profile updating.
- **Security Audit**: Removed personal default credentials and parameterized database passwords via `DB_PASSWORD`.
- **Postman Collection**: Updated environment template to remove deprecated `adminSecretKey`.

---

## [1.3.0] - 2026-10-13

### Added
- **Public Information Website**: Added Home, About, Features, How It Works, and Water Saving pages with responsive blue/white aesthetic.
- **Separate Authentication Interfaces**: Dedicated UI pages for Resident Login (`/resident/login`), Resident Registration (`/resident/register`), Owner Login (`/admin/login`), and Owner Registration (`/admin/register`).
- **Safe Access Denial**: Frontend validation to prevent cross-portal session persistence.

### Changed
- **Navigation & Brand Layout**: Integrated `PublicNavbar` and `PublicFooter` with direct links to Resident and Owner portals.

---

## [1.2.0] - 2026-10-10

### Added
- **Statistical Leak Detection Engine**: 30-day rolling baseline statistical model (mean + $2\sigma$ standard deviation) to identify consumption spikes and meter anomalies.
- **Anomaly Lifecycle Management**: Investigation, confirmation, and resolution states for detected water anomalies.
- **Resident Notification Center**: In-app notifications for leak alerts and published monthly invoices.
- **Peer Benchmarking**: Comparative consumption analytics against apartment averages and normalized occupancy percentiles.
- **Interactive Visualizations**: Recharts daily and monthly consumption charts in Resident Dashboard.

---

## [1.1.0] - 2026-10-09

### Added
- **Progressive Tiered Tariff Engine**: Configurable consumption tiers and rates (e.g., Tier 1: 0–10 kL, Tier 2: >10 kL).
- **Bulk Water Procurement Tracking**: Logging tanker water purchases and tracking volume and supplier expenses.
- **Proportional Shared Cost Allocation**: Distribution of common bulk tanker costs using consumption ratio and area fallback.
- **Billing Cycle State Machine**: Lifecycle enforcement (`OPEN` $\rightarrow$ `CALCULATING` $\rightarrow$ `FINALIZED` $\rightarrow$ `ARCHIVED`).
- **iText PDF Invoicing**: Automated PDF generation for itemized resident water bills.

---

## [1.0.0] - 2026-10-08

### Added
- **Database Architecture**: PostgreSQL schema with Flyway migrations `V1__init_schema.sql` and `V2__complete_platform_schema.sql`.
- **Entity Modeling**: JPA entities for User, Apartment, Household, Meter, and WaterUsageLog.
- **JWT Authentication & RBAC**: Spring Security configuration with stateless JWT token filter and BCrypt password hashing.
- **Community Management**: CRUD APIs for Apartments, Households, and Meters.
- **Water Meter Logging**: Manual meter reading entry and batch CSV upload with validation.
- **React/Vite Frontend**: Core administrative console and basic resident views.
