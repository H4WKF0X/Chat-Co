# Changes

This file records the changes made during the 2026-06-03 auth and route-security session.
Append later changes as new dated sections.

## 2026-06-03

### Spring Security role access

- Switched admin page protection from a manual `beforeEnter` role check to Vaadin/Spring route security with `@RolesAllowed("ADMINISTRATOR")`.
- Enabled Vaadin route annotation enforcement in `SecurityConfig` via `VaadinSecurityConfigurer`.
- Changed normal authenticated Vaadin routes from `@AnonymousAllowed` to `@PermitAll`.
- Kept the LDAP login route anonymous so users can reach the login page.
- Made `/api/auth/login` publicly accessible while keeping other `/api/**` endpoints authenticated.

### User roles and authorities

- Added a shared role mapper to normalize role names between the database, Java domain roles, and Spring Security authorities.
- Added `UserAuthorityService` to convert database roles into Spring Security authorities such as `ROLE_ADMINISTRATOR`.
- Updated LDAP login, LDAP user mapping, and JWT authentication to use the same database-backed authority mapping.
- Updated user role persistence so admin role edits are written to `user_role`, not just displayed in the UI.
- Updated role seed values to use `ADMINISTRATOR`, `MITARBEITER`, and `GAST`.
- Kept compatibility with older role names such as `ADMIN`, `EMPLOYEE`, and `GUEST`.

### Login and provisioning

- Updated Vaadin LDAP login to provision or update the local `AppUser` before creating the Spring Security session.
- Updated API login to provision or update the local `AppUser` before issuing a JWT.
- Fixed a login failure caused by lazy-loading `Role` proxies by querying role names directly.

### Hibernate lazy-loading fixes

- Added a repository query for role names to avoid touching lazy `Role` entities outside a session.
- Added read-only transactions to conversation, message, and meeting read methods that map JPA entities into UI records.
- This fixed lazy proxy failures while building the sidebar, rendering messages, and rendering meetings.

### Conversation type compatibility

- Added case-insensitive parsing for database conversation type values.
- This allows existing rows with values like `channel`, `group`, and `direct` to work with Java enum values `CHANNEL`, `GROUP`, and `DIRECT`.
- Updated meeting creation to store new conversation types as uppercase enum names.

### Verification

- Ran `./mvnw -DskipTests compile` successfully after the code changes.
- Ran `./mvnw test`; compilation completed, but the Spring test context failed because PostgreSQL was not reachable or Hibernate could not determine the dialect from the configured datasource.
- Ran `./mvnw clean compile` successfully to rebuild `target/classes` after a DevTools classloader issue.

## Future Changes

- Add future changes here with date, files touched, reason, and verification command.
