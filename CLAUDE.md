# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Chat-Co is an **on-premise enterprise messenger** (think self-hosted Slack/Teams). Companies run it on their own infrastructure to retain full data sovereignty. Core features: text chat, file transfer, role/permission system, optional voice/video, LDAP/AD user management, and an admin dashboard.

Current branch (`feature/gui-prototyp`) is focused exclusively on building the **Vaadin UI prototype**. No real backend wiring yet — all data must come from a stub/mock layer that can be swapped for a real backend later.

## Commands

```bash
# Run the application (dev mode with live reload)
./mvnw spring-boot:run

# Build (skipping tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ChatCoApplicationTests

# Clean generated Vaadin frontend artifacts
./mvnw vaadin:clean-frontend
```

The app starts at `http://localhost:8080`. Vaadin dev mode opens the browser automatically (`vaadin.launch-browser=true`).

## Architecture

**Stack:** Java 21 · Spring Boot 4.0.2 · Vaadin 25 · PostgreSQL · Lombok

**Package layout:** `com.chatco.chatco`
- `view/` — Vaadin UI views and components (the focus of this branch)
- Future: `model/`, `service/`, `repository/` from the `Database` and `UserAuth` branches

**MVC pattern:** Views in `view/` talk to service interfaces. On this branch those interfaces have stub implementations. When backend branches are merged, the stubs are replaced.

### Stub data pattern

Define a service interface (e.g., `ChatService`) and provide a `StubChatService` implementation annotated `@Service @Profile("dev")` (or as the only bean while no real impl exists). Views inject the interface, never the stub directly. This makes the backend swap a single Spring bean replacement.

### Vaadin conventions

- Views are plain Java classes annotated `@Route("path")`.
- Shared UI structure (sidebar, header) lives in reusable components under `view/components/`.
- Theming: use Vaadin's **Lumo theme** with CSS custom properties for all colors, spacing, and typography. Keep theme overrides in `src/main/frontend/themes/chat-co/` so a designer can change the look by editing one CSS file without touching Java.
- Avoid inline styles (`getStyle().set(...)`). Use CSS class names and the theme stylesheet instead.
- `src/main/frontend/` is **entirely generated** by the Vaadin Maven plugin — never edit generated files.

## Branch Context

| Branch | Content |
|---|---|
| `feature/gui-prototyp` | Vaadin UI (this branch) |
| `Database` | JPA entities, Spring Data repositories, PostgreSQL wiring |
| `UserAuth` | LDAP/AD auth, Spring Security config |
| `Dokumentation` | Project docs (Lastenheft, Machbarkeitsstudie) |

When merging backend branches, `DataSourceAutoConfiguration` can be re-enabled in `ChatCoApplication` (currently excluded to allow the app to start without a DB).

## Key Constraints

- **Java-first UI**: The team has Java expertise and explicitly chose Vaadin to keep UI logic in Java with minimal JS. Do not introduce React, Angular, or heavy JS frameworks.
- **On-premise deployment**: Target is Windows Server + Docker. Keep configuration externalisable via `application.properties`.
- **Theming must be human-editable**: All visual design decisions (colors, fonts, spacing, layout breakpoints) belong in the Lumo CSS theme file, not hardcoded in Java view classes.