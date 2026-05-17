package com.chatco.chatco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "meeting_participant")
/**
 * Join entity connecting users to meetings with their attendance status.
 */
public class MeetingParticipant {
    /** Attendance state such as accepted, declined, or invited. */
    @Size(max = 20)
    @NotNull
    @Column(name = "participant_status", nullable = false, length = 20)
    private String participantStatus;

    /** User side of the composite key. */
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    /** Meeting side of the composite key. */
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /** Composite primary key made from meeting id and user id. */
    @EmbeddedId
    private MeetingParticipantId id;
}
