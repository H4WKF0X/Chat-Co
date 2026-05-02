// dto/MeetingRequest.java
package com.chatco.chatco.dto;

import java.time.OffsetDateTime;

public record MeetingRequest(
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String locationOrLink
) {}