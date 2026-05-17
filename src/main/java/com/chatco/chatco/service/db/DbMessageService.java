package com.chatco.chatco.service.db;

import com.chatco.chatco.model.Conversation;
import com.chatco.chatco.model.Message;
import com.chatco.chatco.model.MessageType;
import com.chatco.chatco.repository.ConversationRepository;
import com.chatco.chatco.repository.MessageRepository;
import com.chatco.chatco.service.MessageService;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DbMessageService implements MessageService {

    private final MessageRepository msgRepo;
    private final ConversationRepository convRepo;
    private final UserService userService;
    private final DbUserService dbUserService;
    private final DbConversationService dbConversationService;

    public DbMessageService(MessageRepository msgRepo,
                            ConversationRepository convRepo,
                            @Lazy UserService userService,
                            DbUserService dbUserService,
                            DbConversationService dbConversationService) {
        this.msgRepo = msgRepo;
        this.convRepo = convRepo;
        this.userService = userService;
        this.dbUserService = dbUserService;
        this.dbConversationService = dbConversationService;
    }

    @Override
    public List<Message> getByConversation(Long conversationId) {
        return msgRepo.findMessageByConversationIdOrderBySentAtAsc(conversationId)
                .stream().map(this::toRecord).toList();
    }

    @Override
    @Transactional
    public Message send(Long conversationId, String content) {
        return persist(conversationId, content, null);
    }

    @Override
    @Transactional
    public Message sendReply(Long conversationId, String content, Long replyToId) {
        return persist(conversationId, content, replyToId);
    }

    @Override
    @Transactional
    public void deleteMessage(Long conversationId, Long messageId) {
        msgRepo.findById(messageId).ifPresent(entity -> {
            entity.setDeletedAt(OffsetDateTime.now());
            msgRepo.save(entity);
        });
    }

    @Override
    @Transactional
    public void editMessage(Long conversationId, Long messageId, String newContent) {
        msgRepo.findById(messageId).ifPresent(entity -> {
            entity.setContent(newContent);
            msgRepo.save(entity);
        });
    }

    private Message persist(Long conversationId, String content, Long replyToId) {
        var senderEntity = new com.chatco.chatco.entity.AppUser();
        senderEntity.setId(userService.getCurrentUser().id());

        var convEntity = convRepo.getReferenceById(conversationId);

        com.chatco.chatco.entity.Message entity = new com.chatco.chatco.entity.Message();
        entity.setConversation(convEntity);
        entity.setSender(senderEntity);
        entity.setContent(content);
        entity.setMessageType("TEXT");
        entity.setSentAt(OffsetDateTime.now());

        if (replyToId != null) {
            msgRepo.findById(replyToId).ifPresent(entity::setReplyToMessage);
        }

        return toRecord(msgRepo.save(entity));
    }

    private Message toRecord(com.chatco.chatco.entity.Message entity) {
        Message replyTo = entity.getReplyToMessage() != null ? toRecord(entity.getReplyToMessage()) : null;
        Conversation conv = entity.getConversation() != null
                ? dbConversationService.toRecord(entity.getConversation()) : null;
        return new Message(
                entity.getId(),
                entity.getContent(),
                safeMessageType(entity.getMessageType()),
                entity.getSentAt(),
                entity.getDeletedAt(),
                dbUserService.toRecord(entity.getSender()),
                conv,
                replyTo
        );
    }

    private MessageType safeMessageType(String raw) {
        try {
            return raw != null ? MessageType.valueOf(raw.toUpperCase()) : MessageType.TEXT;
        } catch (IllegalArgumentException e) {
            return MessageType.TEXT;
        }
    }
}
