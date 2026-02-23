package com.chatco.chatco.repository;

import com.chatco.chatco.entity.ConversationMember;
import com.chatco.chatco.entity.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    List<ConversationMember> findConversationMemberByConversationId(Long conversationId);
    List<ConversationMember> findConversationMemberByUserId(Long userId);
    boolean existsConversationMemberByUserIdAndConversationId(Long userId, Long conversationId);
    void deleteConversationMemberByUserIdAndConversationId(Long userId, Long conversationId);
    void deleteConversationMemberByConversationId(Long conversationId);
    void deleteConversationMemberByUserId(Long userId);
}
