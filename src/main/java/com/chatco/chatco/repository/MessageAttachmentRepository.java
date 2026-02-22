package com.chatco.chatco.repository;

import com.chatco.chatco.entity.MessageAttachment;
import com.chatco.chatco.entity.MessageAttachmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, MessageAttachmentId> {
    List<MessageAttachment> findMessageAttachmentByMessageId(Long messageId);
    List<MessageAttachment> findMessageAttachmentByFileAttachmentId(Long fileAttachmentId);
    boolean existsMessageAttachmentByMessageIdAndFileAttachmentId(Long messageId, Long fileAttachmentId);
    void deleteMessageAttachmentByMessageIdAndFileAttachmentId(Long messageId, Long fileAttachmentId);
    void deleteMessageAttachmentByMessageId(Long messageId);
    void deleteMessageAttachmentByFileAttachmentId(Long fileAttachmentId);
}
