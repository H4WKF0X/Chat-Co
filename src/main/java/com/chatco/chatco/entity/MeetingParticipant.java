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
public class MeetingParticipant {
    @Size(max = 20)
    @NotNull
    @Column(name = "participant_status", nullable = false, length = 20)
    private String participantStatus;
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;
    @EmbeddedId
    private MeetingParticipantId id;
}
