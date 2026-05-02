package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.Message;
import com.chatco.chatco.model.MessageType;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.MessageService;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Dev-profile stub implementation of {@link MessageService}.
 * Messages are stored in-memory and lost on restart. Edits and deletes
 * mutate the list in {@link StubDataStore} by replacing the affected record.
 */
@Service
@Profile("dev")
public class StubMessageService implements MessageService {

    private final StubDataStore store;
    private final UserService userService;
    private final ConversationService conversationService;

    public StubMessageService(StubDataStore store, UserService userService, ConversationService conversationService) {
        this.store = store;
        this.userService = userService;
        this.conversationService = conversationService;
    }

    @Override
    public List<Message> getByConversation(Long conversationId) {
        return store.messagesByConversation.getOrDefault(conversationId, Collections.emptyList());
    }

    @Override
    public void deleteMessage(Long conversationId, Long messageId) {
        List<Message> messages = store.messagesByConversation.getOrDefault(conversationId, Collections.emptyList());
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (m.id().equals(messageId)) {
                messages.set(i, new Message(m.id(), m.content(), m.messageType(), m.sentAt(), OffsetDateTime.now(), m.sender(), m.conversation(), m.replyTo()));
                break;
            }
        }
    }

    @Override
    public void editMessage(Long conversationId, Long messageId, String newContent) {
        List<Message> messages = store.messagesByConversation.getOrDefault(conversationId, Collections.emptyList());
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (m.id().equals(messageId)) {
                messages.set(i, new Message(m.id(), newContent, m.messageType(), m.sentAt(), m.deletedAt(), m.sender(), m.conversation(), m.replyTo()));
                break;
            }
        }
    }

    @Override
    public Message send(Long conversationId, String content) {
        return sendReply(conversationId, content, null);
    }

    @Override
    public Message sendReply(Long conversationId, String content, Long replyToId) {
        var conversation = conversationService.findById(conversationId).orElseThrow();
        Message replyTo = null;
        if (replyToId != null) {
            replyTo = store.messagesByConversation
                    .getOrDefault(conversationId, Collections.emptyList())
                    .stream().filter(m -> m.id().equals(replyToId)).findFirst().orElse(null);
        }
        var message = new Message(
                store.getMessageIdSeq().incrementAndGet(),
                content,
                MessageType.TEXT,
                OffsetDateTime.now(),
                null,
                userService.getCurrentUser(),
                conversation,
                replyTo
        );
        store.messagesByConversation
                .computeIfAbsent(conversationId, id -> Collections.synchronizedList(new java.util.ArrayList<>()))
                .add(message);
        return message;
    }
}