package com.chatco.chatco.repository;

import com.chatco.chatco.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByRoom_Id(Long roomId);
    List<Meeting> findByConversation_Id(Long conversationId);
    List<Meeting> findByStartAtBetween(OffsetDateTime from, OffsetDateTime until);
}