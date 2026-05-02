// dto/MeetingResponse.java
package com.chatco.chatco.dto;

import com.chatco.chatco.entity.Meeting;
import java.time.OffsetDateTime;

public record MeetingResponse(
        Long id,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String locationOrLink
) {
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