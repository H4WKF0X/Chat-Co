package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.*;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.MeetingService;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Dev-profile stub implementation of {@link MeetingService}.
 * Creating a meeting also creates a linked group conversation in
 * {@link StubDataStore} so the meeting chat tab works end-to-end in the UI.
 */
@Service
@Profile("dev")
public class StubMeetingService implements MeetingService {

    private final StubDataStore store;
    private final UserService userService;
    private final ConversationService conversationService;

    public StubMeetingService(StubDataStore store, UserService userService, ConversationService conversationService) {
        this.store = store;
        this.userService = userService;
        this.conversationService = conversationService;
    }

    @Override
    public List<Meeting> getAll() {
        return store.allMeetings;
    }

    @Override
    public Optional<Meeting> findById(Long id) {
        return store.allMeetings.stream().filter(m -> m.id().equals(id)).findFirst();
    }

    @Override
    public List<Meeting> getByUser(Long userId) {
        synchronized (store.allMeetings) {
            return store.allMeetings.stream()
                    .filter(m -> store.participantsByMeeting
                            .getOrDefault(m.id(), Collections.emptyList())
                            .stream().anyMatch(p -> p.user().id().equals(userId)))
                    .toList();
        }
    }

    @Override
    public boolean isRoomAvailable(Room room, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (room == null) return true;
        synchronized (store.allMeetings) {
            return store.allMeetings.stream()
                    .filter(m -> room.equals(m.room()))
                    .noneMatch(m -> startAt.isBefore(m.endAt()) && endAt.isAfter(m.startAt()));
        }
    }

    @Override
    public List<MeetingParticipant> getParticipants(Long meetingId) {
        return store.participantsByMeeting.getOrDefault(meetingId, Collections.emptyList());
    }

    @Override
    public Meeting create(String title, String description, OffsetDateTime startAt, OffsetDateTime endAt,
                          String locationOrLink, Room room, List<Long> participantUserIds) {
        if (!isRoomAvailable(room, startAt, endAt)) {
            throw new IllegalStateException("Room is already booked for this time slot");
        }
        AppUser organiser = userService.getCurrentUser();
        long convId = store.getConversationIdSeq().incrementAndGet();
        Conversation conv = new Conversation(convId, ConversationType.GROUP, title, organiser, OffsetDateTime.now());
        store.allConversations.add(conv);
        store.messagesByConversation.put(convId, Collections.synchronizedList(new ArrayList<>()));

        List<AppUser> convMembers = Collections.synchronizedList(new ArrayList<>());
        convMembers.add(organiser);
        for (Long uid : participantUserIds) {
            if (!uid.equals(organiser.id())) {
                userService.findById(uid).ifPresent(convMembers::add);
            }
        }
        store.membersByConversation.put(convId, convMembers);

        Meeting meeting = new Meeting(
                store.getMeetingIdSeq().incrementAndGet(),
                title, description, startAt, endAt, locationOrLink, room, conv
        );
        store.allMeetings.add(meeting);

        List<MeetingParticipant> participants = Collections.synchronizedList(new ArrayList<>());
        participants.add(new MeetingParticipant(meeting, organiser, ParticipantStatus.ACCEPTED));
        for (Long uid : participantUserIds) {
            if (!uid.equals(organiser.id())) {
                userService.findById(uid).ifPresent(u ->
                        participants.add(new MeetingParticipant(meeting, u, ParticipantStatus.INVITED)));
            }
        }
        store.participantsByMeeting.put(meeting.id(), participants);
        return meeting;
    }

    @Override
    public void updateParticipantStatus(Long meetingId, Long userId, ParticipantStatus status) {
        List<MeetingParticipant> participants = store.participantsByMeeting.get(meetingId);
        if (participants == null) return;
        for (int i = 0; i < participants.size(); i++) {
            MeetingParticipant p = participants.get(i);
            if (p.user().id().equals(userId)) {
                participants.set(i, new MeetingParticipant(p.meeting(), p.user(), status));
                return;
            }
        }
    }
}
