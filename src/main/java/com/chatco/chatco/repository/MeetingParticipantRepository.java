package com.chatco.chatco.repository;

import com.chatco.chatco.entity.MeetingParticipant;
import com.chatco.chatco.entity.MeetingParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for users invited to or attending meetings.
 */
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, MeetingParticipantId> {
    List<MeetingParticipant> findByUser_Id(Long userId);
    List<MeetingParticipant> findByMeeting_Id(Long meetingId);
}
