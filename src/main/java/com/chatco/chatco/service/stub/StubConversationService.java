package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.Conversation;
import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class StubConversationService implements ConversationService {

    private final StubDataStore store;
    private final UserService userService;

    public StubConversationService(StubDataStore store, UserService userService) {
        this.store = store;
        this.userService = userService;
    }

    @Override
    public List<Conversation> getAll() {
        return store.allConversations;
    }

    @Override
    public List<Conversation> getByType(ConversationType type) {
        return store.allConversations.stream()
                .filter(c -> c.type() == type)
                .toList();
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        return store.allConversations.stream()
                .filter(c -> c.id().equals(id))
                .findFirst();
    }

    @Override
    public List<AppUser> getMembers(Long conversationId) {
        return store.membersByConversation.getOrDefault(conversationId, Collections.emptyList());
    }

    @Override
    public Conversation create(ConversationType type, String title, List<Long> memberUserIds) {
        long newId = store.allConversations.stream().mapToLong(c -> c.id()).max().orElse(0) + 1;
        AppUser creator = userService.getCurrentUser();
        Conversation conv = new Conversation(newId, type, title, creator, OffsetDateTime.now());
        store.allConversations.add(conv);

        List<AppUser> members = new ArrayList<>();
        members.add(creator);
        for (Long uid : memberUserIds) {
            if (!uid.equals(creator.id())) {
                userService.findById(uid).ifPresent(members::add);
            }
        }
        store.membersByConversation.put(newId, members);
        store.messagesByConversation.put(newId, new ArrayList<>());
        return conv;
    }
}