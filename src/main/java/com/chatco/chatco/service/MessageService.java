package com.chatco.chatco.service;

import com.chatco.chatco.model.Message;

import java.util.List;

/**
 * Service for sending, editing, deleting, and retrieving chat messages.
 *
 * <p>Deletes are soft: the message record is kept with {@code deletedAt} set.
 * The current implementation is a stub backed by in-memory data.
 * Replace with a JPA-backed implementation when the Database branch is merged.
 */
public interface MessageService {

    /**
     * Returns all messages in a conversation, ordered by send time ascending.
     *
     * @param conversationId the conversation ID
     */
    List<Message> getByConversation(Long conversationId);

    /**
     * Sends a new top-level message in the given conversation.
     *
     * @param conversationId the target conversation
     * @param content        the message text
     * @return the persisted message
     */
    Message send(Long conversationId, String content);

    /**
     * Sends a reply to an existing message.
     *
     * @param conversationId the target conversation
     * @param content        the reply text
     * @param replyToId      ID of the message being replied to
     * @return the persisted reply message
     */
    Message sendReply(Long conversationId, String content, Long replyToId);

    /**
     * Soft-deletes a message by setting its {@code deletedAt} timestamp.
     *
     * @param conversationId the conversation the message belongs to
     * @param messageId      the message to delete
     */
    void deleteMessage(Long conversationId, Long messageId);

    /**
     * Replaces the content of an existing message.
     *
     * @param conversationId the conversation the message belongs to
     * @param messageId      the message to edit
     * @param newContent     the replacement text
     */
    void editMessage(Long conversationId, Long messageId, String newContent);
}
