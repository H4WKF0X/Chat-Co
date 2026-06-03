package com.chatco.chatco.repository;

import com.chatco.chatco.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Database access for meetings and calendar range queries.
 */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByRoom_Id(Long roomId);
    List<Meeting> findByConversation_Id(Long conversationId);
    List<Meeting> findByStartAtBetween(OffsetDateTime from, OffsetDateTime until);

    @Query("SELECT m FROM Meeting m WHERE m.room.id = :roomId AND m.startAt < :endAt AND m.endAt > :startAt")
    List<Meeting> findOverlappingInRoom(
            @Param("roomId") Long roomId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt);
}
