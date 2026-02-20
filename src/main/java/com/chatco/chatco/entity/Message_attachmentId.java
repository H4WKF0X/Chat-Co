package com.chatco.chatco.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class Message_attachmentId implements Serializable {
    private static final long serialVersionUID = 635609960442905902L;
    @NotNull
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @NotNull
    @Column(name = "file_attachment_id", nullable = false)
    private Long fileAttachmentId;


}