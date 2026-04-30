package com.chatco.chatco.model;

import java.time.OffsetDateTime;

/**
 * A conversation between one or more users — either a channel, a direct message, or a group.
 *
 * <p>Meetings also own a linked group conversation created at scheduling time
 * so participants can chat in context of the meeting.
 */
public record Conversation(
        Long id,
        ConversationType type,
        String title,
        AppUser creator,
        OffsetDateTime createdAt
) {}