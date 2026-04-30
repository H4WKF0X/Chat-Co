package com.chatco.chatco.service;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.Conversation;
import com.chatco.chatco.model.ConversationType;

import java.util.List;
import java.util.Optional;

public interface ConversationService {
    List<Conversation> getAll();
    List<Conversation> getByType(ConversationType type);
    Optional<Conversation> findById(Long id);
    List<AppUser> getMembers(Long conversationId);
    Conversation create(ConversationType type, String title, List<Long> memberUserIds);
}