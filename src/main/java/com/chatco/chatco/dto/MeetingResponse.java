// dto/MeetingResponse.java
package com.chatco.chatco.dto;

import com.chatco.chatco.entity.Meeting;
import java.time.OffsetDateTime;

/**
 * API representation of a meeting.
 */
public record MeetingResponse(
        Long id,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String locationOrLink
) {
    /**
     * Converts a JPA meeting entity into the DTO returned by controllers.
     */
    public static MeetingResponse from(Meeting m) {
        return new MeetingResponse(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getStartAt(),
                m.getEndAt(),
                m.getLocationOrLink()
        );
    }
}
