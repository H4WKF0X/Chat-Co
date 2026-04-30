package com.chatco.chatco.model;

import java.time.OffsetDateTime;

public record Conversation(
        Long id,
        ConversationType type,
        String title,
        AppUser creator,
        OffsetDateTime createdAt
) {}