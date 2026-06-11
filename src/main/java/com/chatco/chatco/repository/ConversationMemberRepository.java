package com.chatco.chatco.repository;

import com.chatco.chatco.entity.ConversationMember;
import com.chatco.chatco.entity.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Database access for the many-to-many relation between conversations and users.
 */
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    List<ConversationMember> findConversationMemberByConversationId(Long conversationId);
    List<ConversationMember> findConversationMemberByUserId(Long userId);

    @Query("""
            select cm
            from ConversationMember cm
            join fetch cm.conversation c
            join fetch c.creator
            where cm.user.id = :userId
              and cm.archived = false
            """)
    List<ConversationMember> findActiveByUserId(@Param("userId") Long userId);

    @Query("""
            select cm
            from ConversationMember cm
            join fetch cm.conversation c
            join fetch c.creator
            where cm.user.id = :userId
              and cm.archived = true
            """)
    List<ConversationMember> findArchivedByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ConversationMember cm
            set cm.archived = true
            where cm.conversation.id = :conversationId
            """)
    int archiveByConversationId(@Param("conversationId") Long conversationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ConversationMember cm
            set cm.archived = false
            where cm.conversation.id = :conversationId
              and cm.user.id = :userId
            """)
    int unarchiveByConversationIdAndUserId(@Param("conversationId") Long conversationId,
                                           @Param("userId") Long userId);

    boolean existsConversationMemberByUserIdAndConversationId(Long userId, Long conversationId);
    void deleteConversationMemberByUserIdAndConversationId(Long userId, Long conversationId);
    void deleteConversationMemberByConversationId(Long conversationId);
    void deleteConversationMemberByUserId(Long userId);
}
