package com.chatco.chatco.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "message_attachment")
/**
 * Join entity that attaches an uploaded file to a message.
 */
public class MessageAttachment {
    /** File side of the composite key. */
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "file_attachment_id", nullable = false)
    private FileAttachment fileAttachment;

    /** Message side of the composite key. */
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /** Composite primary key made from message id and file attachment id. */
    @EmbeddedId
    private MessageAttachmentId id;
}
