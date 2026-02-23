-- Chat-Co PostgreSQL Create Script (bereinigt für DataGrip)
-- Variante ohne ENUM-Typen -> weniger Probleme im Editor

-- 1) Benutzer
CREATE TABLE IF NOT EXISTS app_user (
    id              BIGSERIAL PRIMARY KEY,
    ldap_uid        VARCHAR(255) UNIQUE, -- kann NULL sein bei lokaler Registrierung
    username        VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(150) NOT NULL,
    mail            VARCHAR(255) UNIQUE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2) Rollen
CREATE TABLE IF NOT EXISTS role (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE
);

-- 3) Konversationen
CREATE TABLE IF NOT EXISTS conversation (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(20) NOT NULL
                    CHECK (type IN ('direct', 'group', 'channel')),
    title           VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    creator_id      BIGINT NOT NULL,
    CONSTRAINT fk_conversation_creator
        FOREIGN KEY (creator_id)
        REFERENCES app_user(id)
        ON DELETE RESTRICT
);

-- 4) Dateien / Uploads
CREATE TABLE IF NOT EXISTS file_attachment (
    id              BIGSERIAL PRIMARY KEY,
    stored_name     VARCHAR(255) NOT NULL,      -- interner Dateiname
    original_name   VARCHAR(255) NOT NULL,      -- ursprünglicher Dateiname
    mime_type       VARCHAR(150) NOT NULL,      -- z.B. image/png, application/pdf
    size_bytes      BIGINT NOT NULL CHECK (size_bytes >= 0),
    storage_path    TEXT NOT NULL,              -- Pfad im Storage
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    uploaded_by_id  BIGINT NOT NULL,
    CONSTRAINT fk_file_uploaded_by
        FOREIGN KEY (uploaded_by_id)
        REFERENCES app_user(id)
        ON DELETE RESTRICT
);

-- 5) Nachrichten (rekursive Beziehung über reply_to_message_id)
CREATE TABLE IF NOT EXISTS message (
    id                  BIGSERIAL PRIMARY KEY,
    message_type        VARCHAR(20) NOT NULL DEFAULT 'text'
                        CHECK (message_type IN ('text', 'image', 'file', 'system')),
    content             TEXT,
    sent_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,
    sender_id           BIGINT NOT NULL,
    conversation_id     BIGINT NOT NULL,
    reply_to_message_id BIGINT,
    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id)
        REFERENCES app_user(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversation(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_message_reply_to
        FOREIGN KEY (reply_to_message_id)
        REFERENCES message(id)
        ON DELETE SET NULL
);

-- 6) Benutzer <-> Rolle (n:m)
CREATE TABLE IF NOT EXISTS user_role (
    user_id          BIGINT NOT NULL,
    role_id          BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES role(id)
        ON DELETE CASCADE
);

-- 7) Benutzer <-> Konversation (Mitglieder) (n:m)
CREATE TABLE IF NOT EXISTS conversation_member (
    user_id          BIGINT NOT NULL,
    conversation_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, conversation_id),
    CONSTRAINT fk_conv_member_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_conv_member_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversation(id)
        ON DELETE CASCADE
);

-- 8) Nachricht <-> Dateianhang (n:m)
CREATE TABLE IF NOT EXISTS message_attachment (
    message_id           BIGINT NOT NULL,
    file_attachment_id   BIGINT NOT NULL,
    PRIMARY KEY (message_id, file_attachment_id),
    CONSTRAINT fk_msg_attachment_message
        FOREIGN KEY (message_id)
        REFERENCES message(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_msg_attachment_file
        FOREIGN KEY (file_attachment_id)
        REFERENCES file_attachment(id)
        ON DELETE CASCADE
);

-- =========================
-- Indizes (Performance)
-- =========================
CREATE INDEX IF NOT EXISTS idx_message_conversation_id ON message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_sender_id ON message(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_sent_at ON message(sent_at);
CREATE INDEX IF NOT EXISTS idx_message_reply_to_id ON message(reply_to_message_id);

CREATE INDEX IF NOT EXISTS idx_conversation_creator_id ON conversation(creator_id);

CREATE INDEX IF NOT EXISTS idx_file_uploaded_by_id ON file_attachment(uploaded_by_id);
CREATE INDEX IF NOT EXISTS idx_file_uploaded_at ON file_attachment(uploaded_at);

CREATE INDEX IF NOT EXISTS idx_conversation_member_user_id ON conversation_member(user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON user_role(role_id);

-- =========================
-- Optionale Startdaten
-- =========================
INSERT INTO role (name) VALUES
    ('ADMIN'),
    ('EMPLOYEE'),
    ('GUEST')
ON CONFLICT (name) DO NOTHING;