package com.chatco.chatco.model;

import java.time.OffsetDateTime;

public record Message(
        Long id,
        String content,
        MessageType messageType,
        OffsetDateTime sentAt,
        OffsetDateTime deletedAt,
        AppUser sender,
        Conversation conversation,
        Message replyTo
) {}