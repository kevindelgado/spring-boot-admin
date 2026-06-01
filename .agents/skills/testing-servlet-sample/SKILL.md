---
name: testing-servlet-sample
description: Test the Spring Boot Admin servlet sample app end-to-end. Use when verifying UI, login, instance registration, or actuator proxying changes.
---

# Testing Spring Boot Admin Servlet Sample

## Prerequisites

1. **JDK 17** and **Maven** must be installed.
2. Run `mvn -q -DskipTests install` from the repo root first to build all modules.
3. **spring-javaformat**: Before running the app, ensure formatting passes. If `mvn spring-boot:run` fails with formatting violations, run:
   ```bash
   mvn spring-javaformat:apply -f /path/to/repo/pom.xml
   ```
   Then commit the changes. This reformats all modules per the project's code style.

## Starting the App

Run from the **servlet sample module directory** (not the repo root):

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn spring-boot:run -DskipTests
```

The app starts on **port 8080** with the `secure` profile active by default.

**Startup verification checklist:**
- Console shows "Started SpringBootAdminServletApplication" without errors
- No javax/jakarta ClassNotFoundException (if seen, the namespace migration is incomplete)
- No `spring.profiles` deprecation errors (Boot 3 requires `spring.config.activate.on-profile`)
- Log shows "Exposing N endpoint(s) beneath base path '/actuator'" (should be ~21 endpoints)
- Log shows instance registration: "Instance ... is UP" and "ENDPOINTS_DETECTED"

## Test Credentials

- **Username**: `user`
- **Password**: `password`
- These are configured in `application.yml` under the `secure` profile.

## Test Flow

### 1. Login Page
- Navigate to `http://localhost:8080/login`
- Verify: Page renders with "Spring Boot Admin" title, username/password fields, Login button
- This validates **Thymeleaf spring6** template resolution (migrated from spring5)

### 2. Login
- Enter `user` / `password`, click Login
- Verify: Redirected to `/applications` dashboard, no error messages
- This validates **Spring Security 6 SecurityFilterChain** and **CSRF token handling** (CsrfTokenRequestAttributeHandler with CookieCsrfTokenRepository)

### 3. Instance Registration
- On the dashboard, verify:
  - 1 application, 1 instance listed
  - Instance name: `spring-boot-admin-sample-servlet`
  - Status: UP (green checkmark)
  - Version: matches the POM version

### 4. Actuator Proxying (Highest Risk)
- Click on the instance to open the detail view
- **Health section**: Should show Instance UP with sub-components (db, diskSpace, ping)
- **Process section**: PID, uptime, CPU usage visible
- **Threads section**: Live thread chart renders
- **Memory section**: Heap and Non-heap usage with charts
- If health shows errors or "fetch_failed", the **InstancesProxyController** (jakarta.servlet AsyncContext bridging) may be broken

### 5. Metrics
- Click "Metrics" in the left sidebar
- Verify: Dropdown lists ~90 metrics (jvm.*, process.*, system.*, http.*, tomcat.*, spring.security.*)
- Select `jvm.memory.used`, click "Add Metric"
- Verify: A numeric value appears (should be in the millions, representing bytes)

## Common Issues

- **Formatting failures**: The project uses `spring-javaformat-maven-plugin`. Any code changes must pass formatting. Run `mvn spring-javaformat:apply` before building.
- **Port conflicts**: If port 8080 is in use, the app will fail to start. Kill existing processes or change the port in `application.yml`.
- **Notification popup**: Chrome may show a "Show notifications" popup after login. Dismiss it by clicking "Block".
- **Node.js for custom-ui sample**: If testing the custom-ui sample, Node 18 requires `NODE_OPTIONS=--openssl-legacy-provider` for the webpack build.

## Devin Secrets Needed

No secrets required — test credentials are hardcoded in the app's `application.yml`.
