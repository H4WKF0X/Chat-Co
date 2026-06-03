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
- Added a shared database value helper so new conversation rows store lowercase values such as `group`, matching the database check constraint.
- Updated chat and meeting creation to use those lowercase conversation type values.

### Verification

- Ran `./mvnw -DskipTests compile` successfully after the code changes.
- Ran `./mvnw test`; compilation completed, but the Spring test context failed because PostgreSQL was not reachable or Hibernate could not determine the dialect from the configured datasource.
- Ran `./mvnw clean compile` successfully to rebuild `target/classes` after a DevTools classloader issue.

## 2026-06-03 - Message Sending And Live Updates

### Message persistence

- Updated `DbMessageService` so new messages use managed JPA references for the sender and conversation.
- This avoids rendering or persistence problems from ID-only entity objects after a message is saved.
- Message sends, replies, edits, and deletes now trigger a conversation-scoped update event after the transaction commits.
- Fixed message inserts by storing the database-compatible message type value `text` instead of the Java enum-style value `TEXT`.
- Fixed the same enum/database mismatch for new conversations by storing `direct`, `group`, and `channel` instead of uppercase enum names.

### Live chat updates

- Added `MessageBroadcaster`, a small in-memory broadcaster for conversation-specific message updates.
- Enabled Vaadin Push on the existing Vaadin app shell config, `AppConfig`, with `@Push`.
- Updated `ConversationView` to subscribe to updates for the currently open conversation.
- Open chat views now refresh through `UI.access(...)` when another user sends, edits, or deletes a message in the same conversation.
- Added cleanup on view detach so closed chat views are unsubscribed from message updates.
- Fixed the Vaadin startup error caused by placing `@Push` on the Spring Boot application class instead of an app shell class.
- Removed the duplicate app shell source and used `mvn clean compile` so stale compiled app shell classes are removed from `target/classes`.

### Direct message display names

- Added a dynamic display-title helper for conversations.
- Updated the sidebar and chat header so direct messages show the other participant's name for the current user instead of blindly showing `conversation.title`.
- Kept fallback labels for unusual direct messages with missing or incomplete member data.

### Verification

- Ran `./mvnw -DskipTests compile` successfully.

## Future Changes

- Add future changes here with date, files touched, reason, and verification command.
