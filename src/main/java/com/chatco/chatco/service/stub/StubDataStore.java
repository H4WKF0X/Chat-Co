package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.*;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StubDataStore {

    public final AppUser MAX;
    public final AppUser ALEX;
    public final AppUser SEBASTIAN;
    public final AppUser LAURA;
    public final AppUser TOM;
    public final List<AppUser> allUsers;

    public final Conversation GENERAL;
    public final Conversation DEV_TALK;
    public final Conversation ANNOUNCEMENTS;
    public final Conversation DM_ALEX;
    public final Conversation DM_SEBASTIAN;
    public final Conversation GROUP_ALPHA;
    public final Conversation MEETING_SPRINT;
    public final Conversation MEETING_ONBOARDING;
    public final List<Conversation> allConversations;

    public final Map<Long, List<AppUser>> membersByConversation;
    public final Map<Long, List<Message>> messagesByConversation;

    public final Room ROOM_A;
    public final Room ROOM_B;
    public final Room ROOM_C;
    public final List<Room> allRooms;

    public final Meeting MEETING_SPRINT_REVIEW;
    public final Meeting MEETING_ONBOARDING_SESSION;
    public final Meeting MEETING_ARCH_DISCUSSION;
    public final Meeting MEETING_TEAM_LUNCH;
    public final List<Meeting> allMeetings;

    public final Map<Long, List<MeetingParticipant>> participantsByMeeting;

    private final AtomicLong messageIdSeq = new AtomicLong(100);
    private final AtomicLong meetingIdSeq = new AtomicLong(10);

    public StubDataStore() {
        OffsetDateTime base = OffsetDateTime.now().minusMonths(2);

        MAX       = new AppUser(1L, "max.paesold",     "Max Paesold",     "max@chatco.local",       true, UserStatus.ACTIVE,    UserRole.ADMINISTRATOR, base);
        ALEX      = new AppUser(2L, "alex.rutz",       "Alex Rutz",       "alex@chatco.local",      true, UserStatus.ACTIVE,    UserRole.MITARBEITER,   base);
        SEBASTIAN = new AppUser(3L, "sebastian.weigl", "Sebastian Weigl", "sebastian@chatco.local", true, UserStatus.AWAY,     UserRole.MITARBEITER,   base.plusDays(5));
        LAURA     = new AppUser(4L, "laura.fischer",   "Laura Fischer",   "laura@chatco.local",     true, UserStatus.ACTIVE,   UserRole.MITARBEITER,   base.plusWeeks(2));
        TOM       = new AppUser(5L, "tom.becker",      "Tom Becker",      "tom@chatco.local",       false, UserStatus.INACTIVE, UserRole.GAST,          base.plusWeeks(4));
        allUsers  = List.of(MAX, ALEX, SEBASTIAN, LAURA, TOM);

        GENERAL            = new Conversation(1L, ConversationType.CHANNEL, "general",          ALEX, base);
        DEV_TALK           = new Conversation(2L, ConversationType.CHANNEL, "dev-talk",         MAX,  base);
        ANNOUNCEMENTS      = new Conversation(3L, ConversationType.CHANNEL, "announcements",    ALEX, base);
        DM_ALEX            = new Conversation(4L, ConversationType.DIRECT,  "Alex Rutz",        ALEX, base.plusWeeks(1));
        DM_SEBASTIAN       = new Conversation(5L, ConversationType.DIRECT,  "Sebastian Weigl",  SEBASTIAN, base.plusWeeks(3));
        GROUP_ALPHA        = new Conversation(6L, ConversationType.GROUP,   "Project Alpha",    MAX,  base.plusWeeks(2));
        MEETING_SPRINT     = new Conversation(7L, ConversationType.GROUP,   "Sprint Review",    ALEX, base.plusWeeks(3));
        MEETING_ONBOARDING = new Conversation(8L, ConversationType.GROUP,   "Onboarding Session", MAX, base.plusWeeks(5));
        allConversations   = new ArrayList<>(List.of(GENERAL, DEV_TALK, ANNOUNCEMENTS, DM_ALEX, DM_SEBASTIAN, GROUP_ALPHA, MEETING_SPRINT, MEETING_ONBOARDING));

        membersByConversation = new HashMap<>();
        membersByConversation.put(1L, List.of(MAX, ALEX, SEBASTIAN, LAURA, TOM));
        membersByConversation.put(2L, List.of(MAX, ALEX, SEBASTIAN));
        membersByConversation.put(3L, List.of(MAX, ALEX, SEBASTIAN, LAURA, TOM));
        membersByConversation.put(4L, List.of(MAX, ALEX));
        membersByConversation.put(5L, List.of(MAX, SEBASTIAN));
        membersByConversation.put(6L, List.of(MAX, ALEX, SEBASTIAN));
        membersByConversation.put(7L, List.of(MAX, ALEX, SEBASTIAN, LAURA));
        membersByConversation.put(8L, List.of(MAX, ALEX, LAURA, TOM));

        messagesByConversation = new HashMap<>();
        messagesByConversation.put(1L, generalMessages());
        messagesByConversation.put(2L, devTalkMessages());
        messagesByConversation.put(3L, announcementsMessages());
        messagesByConversation.put(4L, dmAlexMessages());
        messagesByConversation.put(5L, dmSebastianMessages());
        messagesByConversation.put(6L, groupAlphaMessages());
        messagesByConversation.put(7L, new ArrayList<>());
        messagesByConversation.put(8L, new ArrayList<>());

        ROOM_A = new Room(1L, "Konferenzraum A", 10, "EG, Raum 101");
        ROOM_B = new Room(2L, "Besprechungsraum B", 4, "1. OG, Raum 212");
        ROOM_C = new Room(3L, "Schulungsraum C", 20, "2. OG, Raum 305");
        allRooms = List.of(ROOM_A, ROOM_B, ROOM_C);

        OffsetDateTime now = OffsetDateTime.now();
        MEETING_SPRINT_REVIEW    = new Meeting(1L, "Sprint Review",          "Präsentation der Sprint-Ergebnisse",       now.plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0),  now.plusDays(1).withHour(15).withMinute(30).withSecond(0).withNano(0),  null,                   ROOM_A, MEETING_SPRINT);
        MEETING_ONBOARDING_SESSION = new Meeting(2L, "Onboarding Session",   "Einführung neuer Mitarbeiter",              now.plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0),  now.plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0),  null,                   ROOM_B, MEETING_ONBOARDING);
        MEETING_ARCH_DISCUSSION  = new Meeting(3L, "Architecture Discussion", "WebSocket vs. SSE für Chat-Modul",         now.plusDays(5).withHour(9).withMinute(30).withSecond(0).withNano(0), now.plusDays(5).withHour(10).withMinute(30).withSecond(0).withNano(0), null,                   null,   DEV_TALK);
        MEETING_TEAM_LUNCH       = new Meeting(4L, "Team Lunch",              "Monatliches Team-Mittagessen",             now.minusDays(2).withHour(12).withMinute(0).withSecond(0).withNano(0), now.minusDays(2).withHour(13).withMinute(0).withSecond(0).withNano(0), "Kantine, EG",         null,   GENERAL);
        allMeetings = new ArrayList<>(List.of(MEETING_SPRINT_REVIEW, MEETING_ONBOARDING_SESSION, MEETING_ARCH_DISCUSSION, MEETING_TEAM_LUNCH));

        participantsByMeeting = new HashMap<>();
        participantsByMeeting.put(1L, new ArrayList<>(List.of(
                new MeetingParticipant(MEETING_SPRINT_REVIEW, MAX,       ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_SPRINT_REVIEW, ALEX,      ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_SPRINT_REVIEW, SEBASTIAN, ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_SPRINT_REVIEW, LAURA,     ParticipantStatus.INVITED)
        )));
        participantsByMeeting.put(2L, new ArrayList<>(List.of(
                new MeetingParticipant(MEETING_ONBOARDING_SESSION, MAX,   ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_ONBOARDING_SESSION, ALEX,  ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_ONBOARDING_SESSION, LAURA, ParticipantStatus.INVITED),
                new MeetingParticipant(MEETING_ONBOARDING_SESSION, TOM,   ParticipantStatus.DECLINED)
        )));
        participantsByMeeting.put(3L, new ArrayList<>(List.of(
                new MeetingParticipant(MEETING_ARCH_DISCUSSION, MAX,       ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_ARCH_DISCUSSION, ALEX,      ParticipantStatus.INVITED),
                new MeetingParticipant(MEETING_ARCH_DISCUSSION, SEBASTIAN, ParticipantStatus.INVITED)
        )));
        participantsByMeeting.put(4L, new ArrayList<>(List.of(
                new MeetingParticipant(MEETING_TEAM_LUNCH, MAX,       ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_TEAM_LUNCH, ALEX,      ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_TEAM_LUNCH, SEBASTIAN, ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_TEAM_LUNCH, LAURA,     ParticipantStatus.ACCEPTED),
                new MeetingParticipant(MEETING_TEAM_LUNCH, TOM,       ParticipantStatus.DECLINED)
        )));
    }

    public AtomicLong getMessageIdSeq() { return messageIdSeq; }
    public AtomicLong getMeetingIdSeq() { return meetingIdSeq; }

    private List<Message> generalMessages() {
        OffsetDateTime d = OffsetDateTime.now().minusDays(2);
        List<Message> m = new ArrayList<>();
        m.add(new Message(10L, "Good morning everyone! Ready for another productive week? 👋",       MessageType.TEXT, d.withHour(9).withMinute(1),  null, ALEX,      GENERAL, null));
        m.add(new Message(11L, "Morning! Yes, lots to do. I'll push the new UI branch today.",        MessageType.TEXT, d.withHour(9).withMinute(5),  null, MAX,       GENERAL, null));
        m.add(new Message(12L, "Don't forget we have the sprint review on Thursday.",                 MessageType.TEXT, d.withHour(9).withMinute(12), null, SEBASTIAN, GENERAL, null));
        m.add(new Message(13L, "Already in the calendar. Should we prepare a quick demo?",           MessageType.TEXT, d.withHour(9).withMinute(15), null, LAURA,     GENERAL, null));
        m.add(new Message(14L, "Great idea! A short live demo will make a much better impression.",  MessageType.TEXT, d.withHour(9).withMinute(16), null, ALEX,      GENERAL, m.get(3)));
        m.add(new Message(15L, "I can cover the DB side if Max handles the UI.",                     MessageType.TEXT, d.withHour(9).withMinute(21), null, SEBASTIAN, GENERAL, null));
        m.add(new Message(16L, "Sounds good. I'll prepare the slides tonight.",                      MessageType.TEXT, d.withHour(10).withMinute(3), null, MAX,       GENERAL, null));
        m.add(new Message(17L, "Reminder: new onboarding docs are in the shared drive.",             MessageType.TEXT, d.plusDays(1).withHour(8).withMinute(45),  null, ALEX,  GENERAL, null));
        m.add(new Message(18L, "Thanks, very helpful for the new members!",                          MessageType.TEXT, d.plusDays(1).withHour(9).withMinute(2),   null, LAURA, GENERAL, null));
        m.add(new Message(19L, "Also pushed a fix for the LDAP connection issue — please pull.",     MessageType.TEXT, d.plusDays(1).withHour(11).withMinute(30), null, MAX,   GENERAL, null));
        return new ArrayList<>(m);
    }

    private List<Message> devTalkMessages() {
        OffsetDateTime d = OffsetDateTime.now().minusDays(1);
        List<Message> m = new ArrayList<>();
        m.add(new Message(20L, "Just merged the entity classes from the Database branch. Check it out!", MessageType.TEXT, d.withHour(10).withMinute(0),  null, MAX,       DEV_TALK, null));
        m.add(new Message(21L, "Looks clean! Quick question — why VARCHAR for the type field instead of a DB enum?", MessageType.TEXT, d.withHour(10).withMinute(15), null, SEBASTIAN, DEV_TALK, null));
        m.add(new Message(22L, "Less migration pain if we add new types later. DataGrip also handles it better.", MessageType.TEXT, d.withHour(10).withMinute(22), null, MAX, DEV_TALK, m.get(1)));
        m.add(new Message(23L, "Makes sense. Did you add a CHECK constraint at the DB level?",       MessageType.TEXT, d.withHour(10).withMinute(30), null, ALEX,      DEV_TALK, null));
        m.add(new Message(24L, "Yes, already in the CREATE script around line 38.",                  MessageType.TEXT, d.withHour(10).withMinute(33), null, MAX,       DEV_TALK, m.get(3)));
        m.add(new Message(25L, "👍 Perfect.",                                                        MessageType.TEXT, d.withHour(10).withMinute(35), null, ALEX,      DEV_TALK, null));
        return new ArrayList<>(m);
    }

    private List<Message> announcementsMessages() {
        OffsetDateTime d = OffsetDateTime.now().minusDays(7);
        List<Message> m = new ArrayList<>();
        m.add(new Message(30L, "Welcome to Chat-Co! This channel is for official announcements only.", MessageType.TEXT, d,              null, ALEX, ANNOUNCEMENTS, null));
        m.add(new Message(31L, "Sprint 1 review scheduled for Thursday 14:00. Link in your calendar.", MessageType.TEXT, d.plusDays(5),  null, ALEX, ANNOUNCEMENTS, null));
        return new ArrayList<>(m);
    }

    private List<Message> dmAlexMessages() {
        OffsetDateTime d = OffsetDateTime.now().minusHours(3);
        List<Message> m = new ArrayList<>();
        m.add(new Message(40L, "Hey Max, are you still handling the Vaadin theme setup?",            MessageType.TEXT, d,                 null, ALEX, DM_ALEX, null));
        m.add(new Message(41L, "Yes, working on it now. Should be done by end of day.",              MessageType.TEXT, d.plusMinutes(5),  null, MAX,  DM_ALEX, null));
        m.add(new Message(42L, "Great! Let me know if you need any input on the design direction.",  MessageType.TEXT, d.plusMinutes(7),  null, ALEX, DM_ALEX, null));
        m.add(new Message(43L, "Will do. I'm going for a Discord-inspired dark theme.",              MessageType.TEXT, d.plusMinutes(10), null, MAX,  DM_ALEX, null));
        m.add(new Message(44L, "Sounds clean. Looking forward to it! 🔥",                           MessageType.TEXT, d.plusMinutes(12), null, ALEX, DM_ALEX, null));
        return new ArrayList<>(m);
    }

    private List<Message> dmSebastianMessages() {
        OffsetDateTime d = OffsetDateTime.now().minusDays(1);
        List<Message> m = new ArrayList<>();
        m.add(new Message(50L, "Sebastian, did you get the test DB running locally?",                MessageType.TEXT, d.withHour(14).withMinute(0),  null, MAX,       DM_SEBASTIAN, null));
        m.add(new Message(51L, "Yeah, finally! Had to update the Docker Compose file — port conflict.", MessageType.TEXT, d.withHour(14).withMinute(8),  null, SEBASTIAN, DM_SEBASTIAN, null));
        m.add(new Message(52L, "Good catch. I'll push that fix to the repo.",                        MessageType.TEXT, d.withHour(14).withMinute(10), null, MAX,       DM_SEBASTIAN, null));
        return new ArrayList<>(m);
    }

    private List<Message> groupAlphaMessages() {
        OffsetDateTime d = OffsetDateTime.now().minusDays(3);
        List<Message> m = new ArrayList<>();
        m.add(new Message(60L, "Welcome to the Project Alpha group! Main coordination channel.",       MessageType.TEXT, d,              null, MAX,       GROUP_ALPHA, null));
        m.add(new Message(61L, "Thanks for setting this up. Initial task list is in the shared drive.", MessageType.TEXT, d.plusHours(1), null, ALEX,      GROUP_ALPHA, null));
        m.add(new Message(62L, "I'll start on the WebSocket integration this week.",                   MessageType.TEXT, d.plusHours(2), null, SEBASTIAN, GROUP_ALPHA, null));
        m.add(new Message(63L, "Perfect. Let's aim for a first working end-to-end test by Friday.",   MessageType.TEXT, d.plusHours(3), null, MAX,       GROUP_ALPHA, null));
        return new ArrayList<>(m);
    }
}
