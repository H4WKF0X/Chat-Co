package com.chatco.chatco.model;

import java.time.OffsetDateTime;

public record Meeting(
        Long id,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String locationOrLink,
        Room room,
        Conversation conversation
) {}