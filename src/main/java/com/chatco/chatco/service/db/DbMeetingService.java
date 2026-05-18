package com.chatco.chatco.service.db;

import com.chatco.chatco.entity.MeetingParticipantId;
import com.chatco.chatco.model.*;
import com.chatco.chatco.repository.MeetingParticipantRepository;
import com.chatco.chatco.repository.MeetingRepository;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.MeetingService;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DbMeetingService implements MeetingService {

    private final MeetingRepository meetingRepo;
    private final MeetingParticipantRepository participantRepo;
    private final UserService userService;
    private final ConversationService conversationService;
    private final DbUserService dbUserService;
    private final DbConversationService dbConversationService;
    private final DbRoomService dbRoomService;

    public DbMeetingService(MeetingRepository meetingRepo,
                            MeetingParticipantRepository participantRepo,
                            @Lazy UserService userService,
                            @Lazy ConversationService conversationService,
                            DbUserService dbUserService,
                            DbConversationService dbConversationService,
                            DbRoomService dbRoomService) {
        this.meetingRepo = meetingRepo;
        this.participantRepo = participantRepo;
        this.userService = userService;
        this.conversationService = conversationService;
        this.dbUserService = dbUserService;
        this.dbConversationService = dbConversationService;
        this.dbRoomService = dbRoomService;
    }

    @Override
    public List<Meeting> getAll() {
        return meetingRepo.findAll().stream().map(this::toRecord).toList();
    }

    @Override
    public Optional<Meeting> findById(Long id) {
        return meetingRepo.findById(id).map(this::toRecord);
    }

    @Override
    public List<Meeting> getByUser(Long userId) {
        return participantRepo.findByUser_Id(userId).stream()
                .map(p -> toRecord(p.getMeeting()))
                .toList();
    }

    @Override
    public List<MeetingParticipant> getParticipants(Long meetingId) {
        return participantRepo.findByMeeting_Id(meetingId).stream()
                .map(this::toParticipantRecord)
                .toList();
    }

    @Override
    @Transactional
    public Meeting create(String title, String description, OffsetDateTime startAt, OffsetDateTime endAt,
                          String locationOrLink, Room room, List<Long> participantUserIds) {
        AppUser organiser = userService.getCurrentUser();

        if (!isRoomAvailable(room, startAt, endAt)) {
            throw new IllegalStateException("Room is already booked for this time slot");
        }

        Conversation conv = conversationService.create(ConversationType.GROUP, title, participantUserIds);

        com.chatco.chatco.entity.Conversation convRef = new com.chatco.chatco.entity.Conversation();
        convRef.setId(conv.id());

        com.chatco.chatco.entity.Meeting meetingEntity = new com.chatco.chatco.entity.Meeting();
        meetingEntity.setTitle(title);
        meetingEntity.setDescription(description);
        meetingEntity.setStartAt(startAt);
        meetingEntity.setEndAt(endAt);
        meetingEntity.setLocationOrLink(locationOrLink);
        meetingEntity.setConversation(convRef);

        if (room != null) {
            com.chatco.chatco.entity.Room roomRef = new com.chatco.chatco.entity.Room();
            roomRef.setId(room.id());
            meetingEntity.setRoom(roomRef);
        }

        com.chatco.chatco.entity.Meeting saved = meetingRepo.save(meetingEntity);

        addParticipant(saved, organiser.id(), ParticipantStatus.ACCEPTED);
        for (Long uid : participantUserIds) {
            if (!uid.equals(organiser.id())) {
                addParticipant(saved, uid, ParticipantStatus.INVITED);
            }
        }

        return toRecord(saved);
    }

    @Override
    @Transactional
    public void updateParticipantStatus(Long meetingId, Long userId, ParticipantStatus status) {
        MeetingParticipantId pid = new MeetingParticipantId();
        pid.setMeetingId(meetingId);
        pid.setUserId(userId);
        participantRepo.findById(pid).ifPresent(entity -> {
            entity.setParticipantStatus(status.name());
            participantRepo.save(entity);
        });
    }

    @Override
    public boolean isRoomAvailable(Room room, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (room == null) return true;
        return meetingRepo.findOverlappingInRoom(room.id(), startAt, endAt).isEmpty();
    }

    private void addParticipant(com.chatco.chatco.entity.Meeting meeting, Long userId, ParticipantStatus status) {
        com.chatco.chatco.entity.AppUser userRef = new com.chatco.chatco.entity.AppUser();
        userRef.setId(userId);
        MeetingParticipantId pid = new MeetingParticipantId();
        pid.setMeetingId(meeting.getId());
        pid.setUserId(userId);
        com.chatco.chatco.entity.MeetingParticipant participant =
                new com.chatco.chatco.entity.MeetingParticipant(pid, meeting, userRef, status.name());
        participantRepo.save(participant);
    }

    private Meeting toRecord(com.chatco.chatco.entity.Meeting entity) {
        Room room = entity.getRoom() != null ? dbRoomService.toRecord(entity.getRoom()) : null;
        Conversation conv = entity.getConversation() != null
                ? dbConversationService.toRecord(entity.getConversation()) : null;
        return new Meeting(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getLocationOrLink(),
                room,
                conv
        );
    }

    private MeetingParticipant toParticipantRecord(com.chatco.chatco.entity.MeetingParticipant entity) {
        Meeting meeting = toRecord(entity.getMeeting());
        AppUser user = dbUserService.toRecord(entity.getUser());
        ParticipantStatus status;
        try {
            status = ParticipantStatus.valueOf(entity.getParticipantStatus());
        } catch (IllegalArgumentException e) {
            status = ParticipantStatus.INVITED;
        }
        return new MeetingParticipant(meeting, user, status);
    }
}
