package com.chatco.chatco.model;

import java.time.OffsetDateTime;

/**
 * A single chat message within a conversation.
 *
 * <p>{@code deletedAt} is {@code null} for live messages; a non-null value
 * indicates a soft delete — the record is kept so reply threads remain intact.
 * {@code replyTo} is {@code null} for top-level messages.
 */
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