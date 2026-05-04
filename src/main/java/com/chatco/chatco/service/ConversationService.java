package com.chatco.chatco.service;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.Conversation;
import com.chatco.chatco.model.ConversationType;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing conversations (channels, direct messages, and groups).
 *
 * <p>The current implementation is a stub backed by in-memory data.
 * Replace with a JPA-backed implementation when the Database branch is merged.
 */
public interface ConversationService {

    /** Returns all conversations regardless of type. */
    List<Conversation> getAll();

    /**
     * Returns all conversations of the given type.
     *
     * @param type the conversation type to filter by
     */
    List<Conversation> getByType(ConversationType type);

    /**
     * Looks up a conversation by its primary key.
     *
     * @param id the conversation ID
     * @return the conversation, or empty if not found
     */
    Optional<Conversation> findById(Long id);

    /**
     * Returns the members of a conversation.
     *
     * @param conversationId the conversation ID
     * @return list of members; empty if the conversation does not exist
     */
    List<AppUser> getMembers(Long conversationId);

    /**
     * Creates a new conversation and registers the given members.
     *
     * <p>The current user is always added as creator and member, even if not
     * included in {@code memberUserIds}.
     * For {@link ConversationType#DIRECT} conversations, returns the existing DM if
     * one already exists between the same pair of users instead of creating a duplicate.
     *
     * @param type          the conversation type
     * @param title         display name of the conversation
     * @param memberUserIds IDs of additional members to add
     * @return the newly created (or existing) conversation
     */
    Conversation create(ConversationType type, String title, List<Long> memberUserIds);

    /**
     * Removes the conversation with the given ID along with all its messages
     * and member entries.
     *
     * @param id the conversation ID to delete
     */
    void deleteById(Long id);
}
