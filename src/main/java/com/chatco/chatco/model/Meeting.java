package com.chatco.chatco.model;

import java.time.OffsetDateTime;

/**
 * A scheduled meeting.
 *
 * <p>{@code room} is {@code null} for remote meetings.
 * {@code locationOrLink} holds either a physical address or a video-call URL;
 * it may be {@code null} when a room is booked and no additional link is needed.
 * {@code conversation} is the group chat created alongside this meeting.
 */
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