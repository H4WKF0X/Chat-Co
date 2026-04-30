package com.chatco.chatco.view;

import com.chatco.chatco.model.*;
import com.chatco.chatco.service.MeetingService;
import com.chatco.chatco.service.RoomService;
import com.chatco.chatco.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Meetings and rooms view.
 *
 * <p>Two tabs: Meetings shows upcoming and past meetings as cards with RSVP
 * controls; Rooms lists bookable rooms. Creating a meeting also creates a linked
 * group conversation. RSVP changes are persisted in-memory via {@link MeetingService}.
 */
@Route(value = "meetings", layout = MainLayout.class)
@AnonymousAllowed
public class MeetingsView extends VerticalLayout {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_HDR = DateTimeFormatter.ofPattern("EEEE, dd. MMMM yyyy");

    private final MeetingService meetingService;
    private final RoomService roomService;
    private final UserService userService;

    private final Div meetingsContent = new Div();
    private final Div roomsContent    = new Div();

    public MeetingsView(MeetingService meetingService, RoomService roomService, UserService userService) {
        this.meetingService = meetingService;
        this.roomService    = roomService;
        this.userService    = userService;

        addClassName("cc-meetings-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

        // Page header
        Span title = new Span("Meetings");
        title.addClassName("cc-page-title");
        Button newMeetingBtn = new Button("New Meeting");
        newMeetingBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        newMeetingBtn.addClickListener(e -> openNewMeetingDialog(null));

        Div pageHeader = new Div(title, newMeetingBtn);
        pageHeader.addClassName("cc-page-header");

        // Tabs
        Tab meetingsTab = new Tab("Meetings");
        Tab roomsTab    = new Tab("Rooms");
        Tabs tabs = new Tabs(meetingsTab, roomsTab);
        tabs.addClassName("cc-settings-tabs");

        // Content area
        Div content = new Div(meetingsContent, roomsContent);
        content.addClassName("cc-meetings-content");

        roomsContent.setVisible(false);
        tabs.addSelectedChangeListener(e -> {
            boolean isMeetings = e.getSelectedTab() == meetingsTab;
            meetingsContent.setVisible(isMeetings);
            roomsContent.setVisible(!isMeetings);
        });

        buildMeetingsList();
        buildRoomsList();

        add(pageHeader, tabs, content);
        expand(content);
    }

    private void buildMeetingsList() {
        meetingsContent.removeAll();
        meetingsContent.addClassName("cc-meetings-list");

        AppUser currentUser = userService.getCurrentUser();
        List<Meeting> all = meetingService.getByUser(currentUser.id());

        OffsetDateTime now = OffsetDateTime.now();
        List<Meeting> upcoming = all.stream().filter(m -> m.endAt().isAfter(now))
                .sorted(Comparator.comparing(Meeting::startAt)).toList();
        List<Meeting> past = all.stream().filter(m -> !m.endAt().isAfter(now))
                .sorted(Comparator.comparing(Meeting::startAt).reversed()).toList();

        if (upcoming.isEmpty() && past.isEmpty()) {
            Span empty = new Span("No meetings scheduled.");
            empty.addClassName("cc-meetings-empty");
            meetingsContent.add(empty);
            return;
        }

        if (!upcoming.isEmpty()) {
            meetingsContent.add(buildSectionHeader("Upcoming"));
            Div upcomingGrid = new Div();
            upcomingGrid.addClassName("cc-meetings-grid");
            upcoming.forEach(m -> upcomingGrid.add(buildMeetingCard(m, currentUser)));
            meetingsContent.add(upcomingGrid);
        }

        if (!past.isEmpty()) {
            Div pastSection = new Div();
            pastSection.addClassName("cc-meetings-past-section");
            Div pastHeader = buildSectionHeader("Past");
            Div pastGrid = new Div();
            pastGrid.addClassName("cc-meetings-grid");
            pastGrid.setVisible(false);

            Button toggle = new Button("Show past (" + past.size() + ")");
            toggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            toggle.addClickListener(e -> {
                boolean nowVisible = !pastGrid.isVisible();
                pastGrid.setVisible(nowVisible);
                toggle.setText(nowVisible ? "Hide past" : "Show past (" + past.size() + ")");
            });

            past.forEach(m -> pastGrid.add(buildMeetingCard(m, currentUser)));
            pastSection.add(pastHeader, toggle, pastGrid);
            meetingsContent.add(pastSection);
        }
    }

    private Div buildSectionHeader(String label) {
        Div h = new Div(new Span(label));
        h.addClassName("cc-meetings-section-header");
        return h;
    }

    private Div buildMeetingCard(Meeting meeting, AppUser currentUser) {
        List<MeetingParticipant> participants = meetingService.getParticipants(meeting.id());

        Span titleSpan = new Span(meeting.title());
        titleSpan.addClassName("cc-meeting-card-title");

        String timeStr = meeting.startAt().format(TIME_FMT) + " – " + meeting.endAt().format(TIME_FMT);
        String dateStr = meeting.startAt().format(DATE_HDR);
        Span timeSpan  = new Span(dateStr + "  ·  " + timeStr);
        timeSpan.addClassName("cc-meeting-card-time");

        String roomLabel = meeting.room() != null ? meeting.room().name() : (meeting.locationOrLink() != null ? meeting.locationOrLink() : "No location");
        Span roomSpan = new Span(roomLabel);
        roomSpan.addClassName("cc-meeting-card-room");

        // Participant avatars (max 4 + overflow)
        Div avatarRow = new Div();
        avatarRow.addClassName("cc-meeting-card-avatars");
        int shown = Math.min(4, participants.size());
        for (int i = 0; i < shown; i++) {
            Avatar av = new Avatar(participants.get(i).user().displayName());
            av.setWidth("24px");
            av.setHeight("24px");
            avatarRow.add(av);
        }
        if (participants.size() > 4) {
            Span overflow = new Span("+" + (participants.size() - 4));
            overflow.addClassName("cc-meeting-card-overflow");
            avatarRow.add(overflow);
        }

        // Own status chip
        participants.stream()
                .filter(p -> p.user().id().equals(currentUser.id()))
                .findFirst()
                .ifPresent(p -> {
                    Span chip = buildStatusChip(p.status());
                    avatarRow.add(chip);
                });

        Div card = new Div(titleSpan, timeSpan, roomSpan, avatarRow);
        card.addClassName("cc-meeting-card");
        card.addClickListener(e -> openMeetingDetail(meeting, participants, currentUser));
        return card;
    }

    private Span buildStatusChip(ParticipantStatus status) {
        String label = switch (status) {
            case ACCEPTED -> "Accepted";
            case INVITED  -> "Invited";
            case DECLINED -> "Declined";
        };
        Span chip = new Span(label);
        chip.addClassName("cc-status-chip");
        chip.addClassName("cc-status-chip--" + status.name().toLowerCase());
        return chip;
    }

    private void openMeetingDetail(Meeting meeting, List<MeetingParticipant> participants, AppUser currentUser) {
        Dialog dialog = new Dialog();
        dialog.setWidth("520px");
        dialog.setHeaderTitle(meeting.title());

        String timeStr = meeting.startAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                + " – " + meeting.endAt().format(DateTimeFormatter.ofPattern("HH:mm"));
        Div timeRow = infoRow("🕐", timeStr);
        Div roomRow = meeting.room() != null
                ? infoRow("🏢", meeting.room().name() + "  (" + meeting.room().location() + ")")
                : (meeting.locationOrLink() != null ? infoRow("📍", meeting.locationOrLink()) : new Div());

        Div descRow = new Div();
        if (meeting.description() != null && !meeting.description().isBlank()) {
            descRow = infoRow("📝", meeting.description());
        }

        // Participants
        Span partLabel = new Span("Participants");
        partLabel.addClassName("cc-detail-section-label");
        Div partList = new Div();
        partList.addClassName("cc-detail-participant-list");
        for (MeetingParticipant p : participants) {
            Avatar av = new Avatar(p.user().displayName());
            av.setWidth("28px");
            av.setHeight("28px");
            String nameText = p.user().displayName() + (p.user().id().equals(currentUser.id()) ? " (You)" : "");
            Span name = new Span(nameText);
            name.addClassName("cc-detail-participant-name");
            Span chip = buildStatusChip(p.status());
            Div row = new Div(av, name, chip);
            row.addClassName("cc-detail-participant-row");
            partList.add(row);
        }

        // Actions
        Button openChat = new Button("Open Chat", e -> {
            dialog.close();
            UI.getCurrent().navigate("conversation/" + meeting.conversation().id());
        });
        openChat.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        // Accept / Decline for current user
        Div myActions = new Div();
        participants.stream()
                .filter(p -> p.user().id().equals(currentUser.id()))
                .findFirst()
                .ifPresent(p -> {
                    if (p.status() != ParticipantStatus.ACCEPTED) {
                        Button accept = new Button("Accept", e -> {
                            meetingService.updateParticipantStatus(meeting.id(), currentUser.id(), ParticipantStatus.ACCEPTED);
                            buildMeetingsList();
                            dialog.close();
                        });
                        accept.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
                        myActions.add(accept);
                    }
                    if (p.status() != ParticipantStatus.DECLINED) {
                        Button decline = new Button("Decline", e -> {
                            meetingService.updateParticipantStatus(meeting.id(), currentUser.id(), ParticipantStatus.DECLINED);
                            buildMeetingsList();
                            dialog.close();
                        });
                        decline.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                        myActions.add(decline);
                    }
                });
        myActions.addClassName("cc-detail-my-actions");

        Button close = new Button("Close", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout footer = new HorizontalLayout(openChat, myActions, close);
        footer.addClassName("cc-detail-footer");

        dialog.add(timeRow, roomRow, descRow, partLabel, partList, footer);
        dialog.open();
    }

    private Div infoRow(String icon, String text) {
        Span ic = new Span(icon);
        ic.addClassName("cc-detail-info-icon");
        Span tx = new Span(text);
        tx.addClassName("cc-detail-info-text");
        Div row = new Div(ic, tx);
        row.addClassName("cc-detail-info-row");
        return row;
    }

    private void openNewMeetingDialog(Room preselectedRoom) {
        Dialog dialog = new Dialog();
        dialog.setWidth("540px");
        dialog.setHeaderTitle("New Meeting");

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();

        TextArea descField = new TextArea("Description (optional)");
        descField.setWidthFull();

        DateTimePicker startPicker = new DateTimePicker("Start");
        startPicker.setValue(LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0));
        startPicker.setWidthFull();

        DateTimePicker endPicker = new DateTimePicker("End");
        endPicker.setValue(LocalDateTime.now().plusDays(1).plusHours(1).withMinute(0).withSecond(0).withNano(0));
        endPicker.setWidthFull();

        TextField locationField = new TextField("Location or link (optional)");
        locationField.setWidthFull();

        ComboBox<Room> roomBox = new ComboBox<>("Room (optional)");
        roomBox.setItems(roomService.getAll());
        roomBox.setItemLabelGenerator(r -> r.name() + " (cap. " + r.capacity() + ")");
        roomBox.setWidthFull();
        if (preselectedRoom != null) roomBox.setValue(preselectedRoom);

        MultiSelectComboBox<AppUser> participantBox = new MultiSelectComboBox<>("Participants");
        AppUser currentUser = userService.getCurrentUser();
        participantBox.setItems(userService.getAll().stream()
                .filter(u -> !u.id().equals(currentUser.id())).toList());
        participantBox.setItemLabelGenerator(AppUser::displayName);
        participantBox.setWidthFull();

        Button create = new Button("Create Meeting");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        create.addClickListener(e -> {
            if (titleField.getValue().isBlank()) {
                titleField.setInvalid(true);
                return;
            }
            OffsetDateTime start = startPicker.getValue().atOffset(ZoneOffset.UTC);
            OffsetDateTime end   = endPicker.getValue().atOffset(ZoneOffset.UTC);
            List<Long> ids = participantBox.getValue().stream().map(AppUser::id).toList();
            meetingService.create(
                    titleField.getValue().trim(),
                    descField.getValue().trim(),
                    start, end,
                    locationField.getValue().trim().isEmpty() ? null : locationField.getValue().trim(),
                    roomBox.getValue(),
                    ids
            );
            buildMeetingsList();
            dialog.close();
            Notification.show("Meeting created", 2000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        dialog.add(titleField, descField, startPicker, endPicker, locationField, roomBox, participantBox,
                new HorizontalLayout(create, cancel));
        dialog.open();
    }

    private void buildRoomsList() {
        roomsContent.removeAll();
        roomsContent.addClassName("cc-rooms-grid");

        for (Room room : roomService.getAll()) {
            Div card = buildRoomCard(room);
            roomsContent.add(card);
        }
    }

    private Div buildRoomCard(Room room) {
        Span name = new Span(room.name());
        name.addClassName("cc-room-card-name");

        Span capacity = new Span("👥 " + room.capacity());
        capacity.addClassName("cc-room-card-capacity");

        Span location = new Span("📍 " + room.location());
        location.addClassName("cc-room-card-location");

        Button bookBtn = new Button("Book");
        bookBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        bookBtn.addClickListener(e -> openNewMeetingDialog(room));

        Div card = new Div(name, capacity, location, bookBtn);
        card.addClassName("cc-room-card");
        return card;
    }
}
