// dto/MeetingRequest.java
package com.chatco.chatco.dto;

import java.time.OffsetDateTime;

/**
 * Request body for creating or updating a meeting.
 */
public record MeetingRequest(
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String locationOrLink
) {}
