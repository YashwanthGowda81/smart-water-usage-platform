# Cybersecurity & Security Architecture Review
**Project:** Smart Water Usage Monitoring and Automated Billing Management Platform  
**Updated:** October 14, 2026  
**Auditor Role:** Cybersecurity Reviewer & Security Architect  

---

## 1. Security Architecture Overview

The Smart Water Platform enforces a multi-layered security model combining Transport Layer Security (in production), stateless token-based authentication via JSON Web Tokens (JWT), robust Role-Based Access Control (RBAC), and fine-grained Household-Level Data Isolation.

```
       [ Client Request ]
               │
               ▼
   [ JwtAuthenticationFilter ] ───> Checks signature & expiration
               │
               ▼
   [ Spring Security RBAC ]    ───> Evaluates role (ADMIN vs RESIDENT)
               │
               ▼
   [ Controller Verification ] ───> Enforces target householdId == user.householdId
               │
               ▼
   [ Business / Service Layer ] ──> Executes calculation / query
```

---

## 2. Threat Analysis & Mitigations

### 2.1 Broken Object Level Authorization (BOLA / IDOR)
- **Vulnerability Risk:** A resident user authenticated as Household `A` submits a request targeting Household `B` (`GET /api/bills/household/{B_ID}` or `GET /api/usage/household/{B_ID}`).
- **Mitigation Implemented:** Controllers for Bills, Meters, Usage, Anomalies, and Benchmarks verify the authenticated principal. If the principal has role `ROLE_RESIDENT`, the target `householdId` parameter is checked against the user's bound `householdId`. Any mismatch triggers an immediate `UnauthorizedException` (HTTP 403 Forbidden).
- **Verified in Code:** `HouseholdBillController.java`, `WaterUsageController.java`, `MeterController.java`, `HouseholdController.java`, `AnomalyEventController.java`, `BenchmarkingController.java`.
- **Automated Test:** `HouseholdDataIsolationTest.java` (4 passing tests).

### 2.2 Privilege Escalation via User Registration & First-Owner Bootstrap
- **Vulnerability Risk:** A malicious actor attempts to register with administrative privileges via `POST /api/auth/register` with `role: "ADMIN"`, or bypasses registration restrictions.
- **Mitigation Implemented:**
  1. **Server-Controlled Dynamic Bootstrap:** The platform uses a dynamic zero-trust bootstrap process via `userRepository.existsByRole(Role.ADMIN)`. The initial administrator can register as `ADMIN` only if zero administrator accounts currently exist in the database.
  2. **Public Admin Lockout:** As soon as an administrator exists, public registration of additional `ADMIN` accounts is permanently disabled on the backend. Any attempt to register as an administrator throws `UnauthorizedException` (HTTP 403). Additional administrative users can only be provisioned internally by existing authenticated administrators.
  3. **Untrusted Client Input Protection:** Resident registration endpoint `POST /api/auth/register/resident` forces `Role.RESIDENT` on the server, completely ignoring any client-supplied role parameter.
- **Verified in Code:** `AuthService.java`, `AuthController.java`.
- **Automated Tests:** `AuthServiceTest.register_SecondAdmin_WhenAdminAlreadyExists_ThrowsUnauthorizedException`, `AuthControllerTest.testRegisterResidentEndpoint_AlwaysForcesResidentRole`.

### 2.3 Strict Portal Role Separation & Login Enforcement
- **Vulnerability Risk:** A Resident attempts to log into the Apartment Owner / Admin console, or an Administrator logs into the Resident portal, leading to credential confusion or unauthorized access.
- **Mitigation Implemented:**
  1. **Server-Side Enforcement:** `LoginRequest` accepts an optional expected `role`. Dedicated endpoints `POST /api/auth/login/owner` (enforces `Role.ADMIN`) and `POST /api/auth/login/resident` (enforces `Role.RESIDENT`) verify that the account's role matches the requested portal before generating or issuing any JWT token.
  2. **Safe Denial:** If a Resident attempts to log in via Owner Login, or an Admin attempts to log in via Resident Login, the backend rejects the request with HTTP 401 `UnauthorizedException` and never generates a JWT.
  3. **Client-Side Defense:** Both `AdminLogin.jsx` and `ResidentLogin.jsx` verify the authenticated role and terminate the session with `logout()` on any role mismatch.
- **Verified in Code:** `AuthService.java`, `AuthController.java`, `AdminLogin.jsx`, `ResidentLogin.jsx`.
- **Automated Tests:** `AuthServiceTest.login_Owner_RejectsResidentAccount_ThrowsUnauthorizedException`, `AuthServiceTest.login_Resident_RejectsAdminAccount_ThrowsUnauthorizedException`.

### 2.4 Credential Storage & Password Hashing
- **Standard Enforced:** Passwords must never be stored in plaintext or exposed in API responses.
- **Implementation:** `SecurityConfig.java` defines a `BCryptPasswordEncoder` bean using salted BCrypt. Registration passes plaintext passwords through `passwordEncoder.encode(request.getPassword())`. Password hashes are excluded from all DTOs and API payloads.
- **Verified in Code:** `UserDto.java`, `AuthResponse.java`, `AuthService.java`.

### 2.5 Hardcoded Secrets & Credential Parameterization
- **Vulnerability Risk:** Embedding default passwords, personal credentials, or static secrets in code repositories.
- **Remediation Implemented:**
  - Removed all personal passwords and static admin secrets.
  - Database credentials parameterized via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
  - JWT HMAC-SHA256 signing key parameterized via `JWT_SECRET`.
  - Environment templates provided via `.env.example` with safe placeholders.

### 2.6 Data Tampering & Negative Meter Readings
- **Vulnerability Risk:** Fraudulent usage entries reporting decreasing readings to manipulate billing charges.
- **Mitigation Implemented:** `WaterUsageService.java` retrieves the latest reading for the meter and enforces `currentReading.compareTo(previousReading) >= 0`. Any non-increasing reading is rejected with `InvalidReadingException`.
- **Verified in Code:** `WaterUsageService.java`, `WaterUsageServiceTest.java`.

### 2.7 Post-Finalization Financial Manipulation
- **Vulnerability Risk:** Modifying water usage records or recalculating bills after a billing cycle has been finalized.
- **Mitigation Implemented:** `BillingCycleService.java` enforces a strict one-way state machine (`OPEN` $\rightarrow$ `CALCULATING` $\rightarrow$ `FINALIZED` $\rightarrow$ `ARCHIVED`). Once marked `FINALIZED`, no recalculation, reading modification, or tariff change can alter the issued bills.
- **Verified in Code:** `BillingCycleService.java`, `BillingCycleServiceTest.java`.

---

## 3. Compliance Summary

| Security Control | Implementation | Audit Verdict |
| :--- | :--- | :--- |
| **Authentication** | Stateless HMAC-SHA256 JWT with configurable expiration | **COMPLIANT** |
| **Password Storage** | Salted BCrypt (adaptive work factor) | **COMPLIANT** |
| **First-Owner Bootstrap** | Server-side check `!userRepository.existsByRole(Role.ADMIN)` | **COMPLIANT** |
| **Portal Separation** | Dual-layer server + client role enforcement on login | **COMPLIANT** |
| **BOLA / IDOR Defense** | Household ownership verified on every resident resource call | **COMPLIANT** |
| **Privilege Escalation** | Admin endpoints require `hasRole('ADMIN')` in Spring Security | **COMPLIANT** |
| **Input Validation** | Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`) | **COMPLIANT** |
| **Data Integrity** | Non-decreasing reading check & billing cycle state machine | **COMPLIANT** |
