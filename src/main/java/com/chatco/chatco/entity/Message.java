package com.chatco.chatco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "message")
public class Message {
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "sender_id", nullable = false)
    private AppUser sender;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;
    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;
    @Size(max = 20)
    @NotNull
    @ColumnDefault("'text'")
    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
}
