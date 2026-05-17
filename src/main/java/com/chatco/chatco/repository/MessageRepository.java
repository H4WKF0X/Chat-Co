package com.chatco.chatco.repository;

import com.chatco.chatco.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for messages in conversations.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findMessageByConversationIdOrderBySentAtAsc(Long conversationId);
    List<Message> findBySenderId(Long senderId);
    long countByConversationId(Long conversationId);
}
