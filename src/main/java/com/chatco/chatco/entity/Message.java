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
/**
 * Single chat message inside a conversation.
 */
public class Message {
    /** Optional parent message when this message is a reply. */
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    /** Conversation that owns this message. Deleting the conversation removes messages. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /** User who sent the message. Users cannot be deleted while messages reference them. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "sender_id", nullable = false)
    private AppUser sender;

    /** Soft-delete timestamp. A null value means the message is still visible. */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /** Time the message was sent. */
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    /** Text content. May be null for attachment-only messages. */
    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    /** Message category, defaulting to {@code text}. */
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
