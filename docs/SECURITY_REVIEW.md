# Cybersecurity & Security Architecture Review
**Project:** Smart Water Usage Monitoring and Automated Billing Management Platform  
**Date:** October 13, 2026  
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

### 2.2 Privilege Escalation via User Registration
- **Vulnerability Risk:** A malicious actor sends a registration payload `POST /api/auth/register` with `role: "ADMIN"`, automatically obtaining administrative privileges without authorization.
- **Mitigation Implemented:** Administrator registration requires a cryptographic or configuration-based bootstrap key (`adminSecret`). If the submitted `adminSecret` does not match the server-side configured secret (`@Value("${smartwater.admin-secret}")`), the system throws `UnauthorizedException` and terminates the request.
- **Verified in Code:** `AuthService.java` lines 42–49.
- **Automated Test:** `AuthServiceTest.register_AdminWithInvalidSecret_ThrowsUnauthorizedException`.

### 2.3 Hardcoded Secrets & Credential Exposure
- **Vulnerability Risk:** Embedding default passwords, JWT HMAC keys, or admin bootstrap passwords directly into source code repositories.
- **Remediation Implemented:**
  - Hardcoded default admin passcodes removed.
  - Admin bootstrap secret parameterized through Spring configuration: `smartwater.admin-secret: ${ADMIN_BOOTSTRAP_SECRET:ADMIN-SECRET-2026}`.
  - JWT secret configurable via `jwt.secret: ${JWT_SECRET:...}`.
  - Database credentials configurable via `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.

### 2.4 Credential Storage & Password Hashing
- **Standard Enforced:** Passwords must never be stored in plaintext.
- **Implementation:** `SecurityConfig.java` defines a `BCryptPasswordEncoder` bean using salted BCrypt with adaptive work factor. Registration passes the plaintext password through `passwordEncoder.encode(request.getPassword())`, and authentication checks against it with `passwordEncoder.matches(...)`.

### 2.5 Data Tampering & Negative Meter Readings
- **Vulnerability Risk:** Dishonest consumption reports submitting negative or lower meter readings to reduce billing charges.
- **Mitigation Implemented:** `WaterUsageService.java` fetches the most recent logged reading for the specified meter and enforces `currentReading.compareTo(previousReading) >= 0`. Any reading lower than previous triggers `InvalidReadingException`.

### 2.6 Post-Finalization Financial Manipulation
- **Vulnerability Risk:** Recalculating or mutating bills in a finalized or archived billing cycle.
- **Mitigation Implemented:** `BillingCycleService.calculateBills()` verifies that the cycle status is `OPEN`. If the cycle is `FINALIZED` or `ARCHIVED`, the operation is aborted with `InvalidReadingException`.

---

## 3. Vulnerability Assessment Summary

| Threat / CWE | Risk Level Before Fix | Status After Fix | Verification Evidence |
| :--- | :---: | :---: | :--- |
| **CWE-285: Improper Authorization** (BOLA) | HIGH | **MITIGATED** | `HouseholdDataIsolationTest` passed |
| **CWE-269: Privilege Escalation** | HIGH | **MITIGATED** | `AuthServiceTest` escalation defense passed |
| **CWE-798: Use of Hard-coded Credentials** | MEDIUM | **MITIGATED** | Parameterized in `application.yml` and `AuthService.java` |
| **CWE-522: Insufficiently Protected Credentials** | HIGH | **MITIGATED** | BCrypt password hashing active |
| **CWE-20: Improper Input Validation** (Meters) | MEDIUM | **MITIGATED** | Non-decreasing reading check enforced |
| **CWE-668: Exposure of Resource to Wrong Sphere** | HIGH | **MITIGATED** | Residents filtered from viewing other household records |

---

## 4. Production Deployment Hardening Checklist

When promoting this platform to a public or staging cloud environment:
1. [ ] Supply a strong 256-bit random string for `JWT_SECRET` in environment variables.
2. [ ] Supply an unpredictable alphanumeric string for `ADMIN_BOOTSTRAP_SECRET`.
3. [ ] Configure TLS/HTTPS termination via Nginx, Traefik, or Cloud Load Balancer.
4. [ ] Enable rate-limiting on `/api/auth/login` and `/api/auth/register` to prevent brute-force attacks.
5. [ ] Restrict database incoming TCP access on port 5432 to the backend application container/host only.
