package com.chatco.chatco.service;

import com.chatco.chatco.model.Meeting;
import com.chatco.chatco.model.MeetingParticipant;
import com.chatco.chatco.model.ParticipantStatus;
import com.chatco.chatco.model.Room;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for scheduling and managing meetings.
 *
 * <p>Creating a meeting also creates an accompanying group conversation so
 * participants can chat in context. The current implementation is a stub
 * backed by in-memory data. Replace with a JPA-backed implementation when
 * the Database branch is merged.
 */
public interface MeetingService {

    /** Returns all meetings regardless of date or participant. */
    List<Meeting> getAll();

    /**
     * Looks up a meeting by its primary key.
     *
     * @param id the meeting ID
     * @return the meeting, or empty if not found
     */
    Optional<Meeting> findById(Long id);

    /**
     * Returns all meetings the given user is a participant in.
     *
     * @param userId the user ID to filter by
     */
    List<Meeting> getByUser(Long userId);

    /**
     * Returns the participant list for a meeting, including each user's RSVP status.
     *
     * @param meetingId the meeting ID
     */
    List<MeetingParticipant> getParticipants(Long meetingId);

    /**
     * Creates a new meeting.
     *
     * <p>The current user is added as organiser with {@code ACCEPTED} status.
     * All other participants are added with {@code INVITED} status.
     * A group conversation is created alongside the meeting for participant chat.
     *
     * @param title              meeting title
     * @param description        optional description or agenda
     * @param startAt            start date and time
     * @param endAt              end date and time
     * @param locationOrLink     physical address or video-call URL; may be null
     * @param room               booked room; null for remote meetings
     * @param participantUserIds IDs of users to invite
     * @return the newly created meeting
     */
    Meeting create(String title, String description, OffsetDateTime startAt, OffsetDateTime endAt,
                   String locationOrLink, Room room, List<Long> participantUserIds);

    /**
     * Updates a participant's RSVP status for a meeting.
     *
     * @param meetingId the meeting ID
     * @param userId    the participant's user ID
     * @param status    the new status (ACCEPTED or DECLINED)
     */
    void updateParticipantStatus(Long meetingId, Long userId, ParticipantStatus status);

    /**
     * Returns true if the given room has no overlapping bookings in the specified window.
     * Always returns true when room is null (remote/no-room meetings).
     */
    boolean isRoomAvailable(Room room, OffsetDateTime startAt, OffsetDateTime endAt);
}