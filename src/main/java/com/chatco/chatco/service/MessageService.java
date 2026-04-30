package com.chatco.chatco.service;

import com.chatco.chatco.model.Message;

import java.util.List;

public interface MessageService {
    List<Message> getByConversation(Long conversationId);
    Message send(Long conversationId, String content);
    Message sendReply(Long conversationId, String content, Long replyToId);
    void deleteMessage(Long conversationId, Long messageId);
    void editMessage(Long conversationId, Long messageId, String newContent);
}