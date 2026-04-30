package com.chatco.chatco.service;

import com.chatco.chatco.model.Meeting;
import com.chatco.chatco.model.MeetingParticipant;
import com.chatco.chatco.model.ParticipantStatus;
import com.chatco.chatco.model.Room;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingService {
    List<Meeting> getAll();
    Optional<Meeting> findById(Long id);
    List<Meeting> getByUser(Long userId);
    List<MeetingParticipant> getParticipants(Long meetingId);
    Meeting create(String title, String description, OffsetDateTime startAt, OffsetDateTime endAt,
                   String locationOrLink, Room room, List<Long> participantUserIds);
    void updateParticipantStatus(Long meetingId, Long userId, ParticipantStatus status);
}