package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.*;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.MeetingService;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

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
        return store.allMeetings.stream()
                .filter(m -> store.participantsByMeeting
                        .getOrDefault(m.id(), Collections.emptyList())
                        .stream().anyMatch(p -> p.user().id().equals(userId)))
                .toList();
    }

    @Override
    public List<MeetingParticipant> getParticipants(Long meetingId) {
        return store.participantsByMeeting.getOrDefault(meetingId, Collections.emptyList());
    }

    @Override
    public Meeting create(String title, String description, OffsetDateTime startAt, OffsetDateTime endAt,
                          String locationOrLink, Room room, List<Long> participantUserIds) {
        AppUser organiser = userService.getCurrentUser();
        long convId = store.allConversations.stream().mapToLong(Conversation::id).max().orElse(0) + 1;
        Conversation conv = new Conversation(convId, ConversationType.GROUP, title, organiser, OffsetDateTime.now());
        store.allConversations.add(conv);
        store.messagesByConversation.put(convId, new ArrayList<>());

        List<AppUser> convMembers = new ArrayList<>();
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

        List<MeetingParticipant> participants = new ArrayList<>();
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
