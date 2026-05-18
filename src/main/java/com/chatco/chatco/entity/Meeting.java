package com.chatco.chatco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "meeting")
/**
 * Calendar meeting linked to a group conversation.
 */
public class Meeting {
    /** Backing conversation for meeting chat. Deleting it deletes the meeting. */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /** Optional physical room. If the room is removed, the meeting remains without it. */
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "room_id")
    private Room room;

    /** Physical location or online meeting link shown to participants. */
    @Size(max = 500)
    @Column(name = "location_or_link", length = 500)
    private String locationOrLink;

    /** End timestamp of the meeting. */
    @NotNull
    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    /** Start timestamp of the meeting. */
    @NotNull
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    /** Optional longer meeting notes. */
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    /** Short meeting title used in calendar views. */
    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

}
