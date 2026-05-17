-- Chat-Co PostgreSQL Create Script (erweitert für DataGrip)
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

-- 4) Räume
CREATE TABLE IF NOT EXISTS room (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL UNIQUE,
    capacity        INTEGER NOT NULL CHECK (capacity > 0),
    location        VARCHAR(255) NOT NULL
);

-- 5) Dateien / Uploads
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

-- 6) Meetings
CREATE TABLE IF NOT EXISTS meeting (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    start_at            TIMESTAMPTZ NOT NULL,
    end_at              TIMESTAMPTZ NOT NULL,
    location_or_link    VARCHAR(500),
    room_id             BIGINT,
    conversation_id     BIGINT NOT NULL UNIQUE,
    CONSTRAINT chk_meeting_time
        CHECK (end_at > start_at),
    CONSTRAINT fk_meeting_room
        FOREIGN KEY (room_id)
        REFERENCES room(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_meeting_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversation(id)
        ON DELETE CASCADE
);

-- 7) Nachrichten (rekursive Beziehung über reply_to_message_id)
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

-- 8) Benutzer <-> Rolle (n:m)
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

-- 9) Benutzer <-> Konversation (Mitglieder) (n:m)
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

-- 10) Nachricht <-> Dateianhang (n:m)
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

-- 11) Meeting <-> Teilnehmer (n:m)
CREATE TABLE IF NOT EXISTS meeting_participant (
    meeting_id           BIGINT NOT NULL,
    user_id              BIGINT NOT NULL,
    participant_status   VARCHAR(20) NOT NULL
                         CHECK (participant_status IN ('invited', 'accepted', 'declined', 'tentative')),
    PRIMARY KEY (meeting_id, user_id),
    CONSTRAINT fk_meeting_participant_meeting
        FOREIGN KEY (meeting_id)
        REFERENCES meeting(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_meeting_participant_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE
);
-- =========================
-- Optionale Startdaten
-- =========================
INSERT INTO role (name) VALUES
    ('ADMIN'),
    ('EMPLOYEE'),
    ('GUEST')
ON CONFLICT (name) DO NOTHING;