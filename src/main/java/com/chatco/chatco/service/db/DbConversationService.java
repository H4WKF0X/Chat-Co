package com.chatco.chatco.service.db;

import com.chatco.chatco.entity.ConversationMember;
import com.chatco.chatco.entity.ConversationMemberId;
import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.Conversation;
import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.repository.ConversationMemberRepository;
import com.chatco.chatco.repository.ConversationRepository;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DbConversationService implements ConversationService {

    private final ConversationRepository convRepo;
    private final ConversationMemberRepository memberRepo;
    private final UserService userService;
    private final DbUserService dbUserService;

    public DbConversationService(ConversationRepository convRepo,
                                 ConversationMemberRepository memberRepo,
                                 @Lazy UserService userService,
                                 DbUserService dbUserService) {
        this.convRepo = convRepo;
        this.memberRepo = memberRepo;
        this.userService = userService;
        this.dbUserService = dbUserService;
    }

    @Override
    public List<Conversation> getAll() {
        AppUser current = userService.getCurrentUser();
        return memberRepo.findConversationMemberByUserId(current.id()).stream()
                .map(cm -> toRecord(cm.getConversation()))
                .toList();
    }

    @Override
    public List<Conversation> getByType(ConversationType type) {
        return getAll().stream().filter(c -> c.type() == type).toList();
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        return convRepo.findById(id).map(this::toRecord);
    }

    @Override
    public List<AppUser> getMembers(Long conversationId) {
        return memberRepo.findConversationMemberByConversationId(conversationId).stream()
                .map(cm -> dbUserService.toRecord(cm.getUser()))
                .toList();
    }

    @Override
    @Transactional
    public Conversation create(ConversationType type, String title, List<Long> memberUserIds) {
        AppUser creator = userService.getCurrentUser();

        if (type == ConversationType.DIRECT) {
            Optional<Conversation> existing = findExistingDirect(creator.id(), memberUserIds);
            if (existing.isPresent()) return existing.get();
        }

        com.chatco.chatco.entity.AppUser creatorEntity = new com.chatco.chatco.entity.AppUser();
        creatorEntity.setId(creator.id());

        com.chatco.chatco.entity.Conversation convEntity = new com.chatco.chatco.entity.Conversation();
        convEntity.setType(type.name());
        convEntity.setTitle(title);
        convEntity.setCreator(creatorEntity);
        convEntity.setCreatedAt(OffsetDateTime.now());
        com.chatco.chatco.entity.Conversation saved = convRepo.save(convEntity);

        addMember(saved, creator.id());
        Set<Long> added = new HashSet<>();
        added.add(creator.id());
        for (Long uid : memberUserIds) {
            if (added.add(uid)) addMember(saved, uid);
        }

        return toRecord(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        memberRepo.deleteConversationMemberByConversationId(id);
        convRepo.deleteById(id);
    }

    private Optional<Conversation> findExistingDirect(Long currentUserId, List<Long> memberUserIds) {
        return memberRepo.findConversationMemberByUserId(currentUserId).stream()
                .filter(cm -> "DIRECT".equals(cm.getConversation().getType()))
                .filter(cm -> {
                    Long otherId = memberUserIds.stream()
                            .filter(id -> !id.equals(currentUserId))
                            .findFirst().orElse(null);
                    return otherId != null && memberRepo
                            .existsConversationMemberByUserIdAndConversationId(otherId, cm.getConversation().getId());
                })
                .map(cm -> toRecord(cm.getConversation()))
                .findFirst();
    }

    private void addMember(com.chatco.chatco.entity.Conversation conv, Long userId) {
        com.chatco.chatco.entity.AppUser userRef = new com.chatco.chatco.entity.AppUser();
        userRef.setId(userId);
        ConversationMemberId memberId = new ConversationMemberId();
        memberId.setConversationId(conv.getId());
        memberId.setUserId(userId);
        ConversationMember member = new ConversationMember(memberId, conv, userRef);
        memberRepo.save(member);
    }

    Conversation toRecord(com.chatco.chatco.entity.Conversation entity) {
        AppUser creator = entity.getCreator() != null
                ? dbUserService.toRecord(entity.getCreator())
                : null;
        return new Conversation(
                entity.getId(),
                ConversationType.valueOf(entity.getType()),
                entity.getTitle(),
                creator,
                entity.getCreatedAt()
        );
    }
}
