package com.chatco.chatco.repository;

import com.chatco.chatco.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findConversationByType(String type);
    List<Conversation> findConversationByTitle(String title);
    boolean existsByTitle(String title);
}
