package com.chatco.chatco.view;

import com.chatco.chatco.model.*;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.MessageService;
import com.chatco.chatco.service.UserService;
import com.chatco.chatco.view.components.UserDetailDialog;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main chat view for a single conversation.
 *
 * <p>Displays the message list with date separators, a reply-to bar, and the
 * message input. Supports inline message editing and soft-delete. An optional
 * members panel slides in from the right when the Members button is clicked.
 * The view is re-entered on every navigation to the same or a different
 * conversation, at which point {@link #beforeEnter} reloads data.
 */
@Route(value = "conversation/:id", layout = MainLayout.class)
@AnonymousAllowed
public class ConversationView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd. MMM yyyy");

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final UserService userService;

    private final Span headerIcon  = new Span();
    private final Span headerTitle = new Span();
    private final Span headerMeta  = new Span();
    private final Div  messageArea = new Div();
    private final Div  memberPanel = new Div();
    private boolean memberPanelOpen = false;

    private Conversation conversation;
    private Message pendingReplyTo = null;

    private final Div  replyBar        = new Div();
    private final Span replyBarSender  = new Span();
    private final Span replyBarPreview = new Span();

    public ConversationView(MessageService messageService, ConversationService conversationService, UserService userService) {
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.userService = userService;

        addClassName("cc-chat-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        HorizontalLayout body = new HorizontalLayout();
        body.setSizeFull();
        body.setPadding(false);
        body.setSpacing(false);
        body.addClassName("cc-chat-body");

        Div chatColumn = new Div(buildMessageArea(), buildInputBar());
        chatColumn.addClassName("cc-chat-column");

        memberPanel.addClassName("cc-member-panel");
        memberPanel.setVisible(false);

        body.add(chatColumn, memberPanel);
        body.expand(chatColumn);

        add(buildHeader(), body);
        expand(body);
    }

    private Div buildHeader() {
        headerIcon.addClassName("cc-chat-header-icon");
        headerTitle.addClassName("cc-chat-header-title");
        headerMeta.addClassName("cc-chat-header-meta");

        Button membersBtn = new Button("👥 Members");
        membersBtn.addClassName("cc-members-btn");
        membersBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        membersBtn.addClickListener(e -> toggleMemberPanel());

        Div left  = new Div(headerIcon, headerTitle, headerMeta);
        left.addClassName("cc-chat-header-left");
        Div right = new Div(membersBtn);
        right.addClassName("cc-chat-header-right");

        Div header = new Div(left, right);
        header.addClassName("cc-chat-header");
        return header;
    }

    private Div buildMessageArea() {
        messageArea.addClassName("cc-message-list-wrapper");
        messageArea.setSizeFull();
        return messageArea;
    }

    private Div buildInputBar() {
        Span replyLabel = new Span("↩ Replying to ");
        replyLabel.addClassName("cc-reply-bar-label");
        replyBarSender.addClassName("cc-reply-bar-sender");
        replyBarPreview.addClassName("cc-reply-bar-preview");

        Div replyBarText = new Div(replyLabel, replyBarSender, new Span(": "), replyBarPreview);
        replyBarText.addClassName("cc-reply-bar-text");

        Button cancelReply = new Button("×");
        cancelReply.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        cancelReply.addClickListener(e -> clearReplyTarget());

        replyBar.addClassName("cc-reply-bar");
        replyBar.add(replyBarText, cancelReply);
        replyBar.setVisible(false);

        MessageInput input = new MessageInput();
        input.setWidthFull();
        input.addSubmitListener(e -> {
            if (conversation != null && !e.getValue().isBlank()) {
                if (pendingReplyTo != null) {
                    messageService.sendReply(conversation.id(), e.getValue(), pendingReplyTo.id());
                    clearReplyTarget();
                } else {
                    messageService.send(conversation.id(), e.getValue());
                }
                refreshMessages();
            }
        });

        Div bar = new Div(replyBar, input);
        bar.addClassName("cc-message-input-bar");
        bar.setWidthFull();
        return bar;
    }

    private void setReplyTarget(Message m) {
        pendingReplyTo = m;
        replyBarSender.setText(m.sender().displayName());
        String preview = m.content().length() > 60 ? m.content().substring(0, 60) + "…" : m.content();
        replyBarPreview.setText(preview);
        replyBar.setVisible(true);
    }

    private void clearReplyTarget() {
        pendingReplyTo = null;
        replyBar.setVisible(false);
    }

    private void toggleMemberPanel() {
        memberPanelOpen = !memberPanelOpen;
        memberPanel.setVisible(memberPanelOpen);
        if (memberPanelOpen && conversation != null) {
            rebuildMemberPanel();
        }
    }

    private void rebuildMemberPanel() {
        memberPanel.removeAll();
        AppUser currentUser = userService.getCurrentUser();

        Div panelHeader = new Div(new Span("Members"));
        panelHeader.addClassName("cc-member-panel-header");
        memberPanel.add(panelHeader);

        List<AppUser> members = conversationService.getMembers(conversation.id());
        for (AppUser member : members) {
            Div row = buildMemberRow(member, currentUser);
            memberPanel.add(row);
        }
    }

    private Div buildMemberRow(AppUser member, AppUser currentUser) {
        Avatar avatar = new Avatar(member.displayName());
        avatar.setWidth("32px");
        avatar.setHeight("32px");

        Span name = new Span(member.displayName() + (member.id().equals(currentUser.id()) ? " (You)" : ""));
        name.addClassName("cc-member-name");

        Span dot = new Span();
        dot.addClassName("cc-status-dot");
        dot.addClassName("cc-status-dot--" + member.status().name().toLowerCase());

        Div row = new Div(avatar, name, dot);
        row.addClassName("cc-member-row");
        if (!member.id().equals(currentUser.id())) {
            row.addClickListener(e -> new UserDetailDialog(member, conversationService).open());
        }
        return row;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong)
                .flatMap(conversationService::findById)
                .ifPresentOrElse(conv -> {
                    conversation = conv;
                    clearReplyTarget();
                    updateHeader(conv);
                    refreshMessages();
                    if (memberPanelOpen) rebuildMemberPanel();
                }, () -> event.forwardTo(EmptyView.class));
    }

    private void updateHeader(Conversation conv) {
        headerIcon.setText(conv.type() == ConversationType.CHANNEL ? "#" : conv.type() == ConversationType.GROUP ? "⊞" : "");
        headerTitle.setText(conv.title());
        headerMeta.setText(switch (conv.type()) {
            case CHANNEL -> "channel";
            case DIRECT  -> "direct message";
            case GROUP   -> "group";
        });
    }

    private void refreshMessages() {
        messageArea.removeAll();
        List<Message> messages = messageService.getByConversation(conversation.id());
        AppUser currentUser = userService.getCurrentUser();

        LocalDate lastDate = null;
        for (Message m : messages) {
            LocalDate msgDate = m.sentAt().toLocalDate();
            if (!msgDate.equals(lastDate)) {
                messageArea.add(buildDateSeparator(msgDate));
                lastDate = msgDate;
            }
            messageArea.add(buildMessageRow(m, currentUser));
        }
    }

    private Div buildDateSeparator(LocalDate date) {
        String label;
        LocalDate today = LocalDate.now();
        if (date.equals(today))              label = "Today";
        else if (date.equals(today.minusDays(1))) label = "Yesterday";
        else                                  label = date.format(DATE_FMT);

        Span text = new Span(label);
        text.addClassName("cc-date-sep-label");
        Div sep = new Div(text);
        sep.addClassName("cc-date-separator");
        return sep;
    }

    private Div buildMessageRow(Message m, AppUser currentUser) {
        boolean isOwn    = m.sender().id().equals(currentUser.id());
        boolean isDeleted = m.deletedAt() != null;

        Avatar avatar = new Avatar(m.sender().displayName());
        avatar.setWidth("36px");
        avatar.setHeight("36px");
        avatar.setAbbreviation(initials(m.sender().displayName()));
        avatar.addClassName("cc-msg-avatar");

        Div avatarWrapper = new Div(avatar);
        avatarWrapper.addClassName("cc-msg-avatar-wrap");
        if (!isOwn) {
            avatarWrapper.addClickListener(e -> new UserDetailDialog(m.sender(), conversationService).open());
            avatarWrapper.addClassName("cc-clickable");
        }

        Span senderName = new Span(m.sender().displayName());
        senderName.addClassName("cc-msg-sender");
        if (!isOwn) {
            senderName.addClickListener(e -> new UserDetailDialog(m.sender(), conversationService).open());
            senderName.addClassName("cc-clickable");
        }

        Span timestamp = new Span(m.sentAt().format(TIME_FMT));
        timestamp.addClassName("cc-msg-time");

        Div meta = new Div(senderName, timestamp);
        meta.addClassName("cc-msg-meta");

        Div contentArea = new Div();
        contentArea.addClassName("cc-msg-content-area");

        if (m.replyTo() != null && !isDeleted) {
            contentArea.add(buildReplyStrip(m.replyTo()));
        }

        Span content = new Span(isDeleted ? "[Message deleted]" : m.content());
        content.addClassName(isDeleted ? "cc-msg-deleted" : "cc-msg-content");
        contentArea.add(content);

        Div bubble = new Div(meta, contentArea);
        bubble.addClassName("cc-msg-bubble");

        Div row = new Div(avatarWrapper, bubble);
        row.addClassName("cc-msg-row");
        if (isOwn) row.addClassName("cc-msg-row--own");

        if (!isDeleted) {
            Div actions = buildMessageActions(m, isOwn);
            row.add(actions);
            row.addClassName("cc-msg-row--has-actions");
        }

        return row;
    }

    private Div buildReplyStrip(Message original) {
        Span sender = new Span(original.sender().displayName() + ": ");
        sender.addClassName("cc-reply-sender");
        String preview = original.content().length() > 80
                ? original.content().substring(0, 80) + "…"
                : original.content();
        Span text = new Span(preview);
        text.addClassName("cc-reply-text");
        Div strip = new Div(sender, text);
        strip.addClassName("cc-msg-reply-strip");
        return strip;
    }

    private Div buildMessageActions(Message m, boolean isOwn) {
        Div actions = new Div();
        actions.addClassName("cc-msg-actions");

        Button replyBtn = new Button("↩");
        replyBtn.addClassName("cc-msg-action-btn");
        replyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        replyBtn.getElement().setAttribute("title", "Reply");
        replyBtn.addClickListener(e -> setReplyTarget(m));
        actions.add(replyBtn);

        if (isOwn) {
            Button editBtn = new Button("✏");
            editBtn.addClassName("cc-msg-action-btn");
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
            editBtn.getElement().setAttribute("title", "Edit");
            editBtn.addClickListener(e -> openInlineEdit(m));

            Button deleteBtn = new Button("🗑");
            deleteBtn.addClassName("cc-msg-action-btn");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
            deleteBtn.getElement().setAttribute("title", "Delete");
            deleteBtn.addClickListener(e -> {
                messageService.deleteMessage(conversation.id(), m.id());
                refreshMessages();
            });

            actions.add(editBtn, deleteBtn);
        }

        return actions;
    }

    private void openInlineEdit(Message m) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit message");

        TextField field = new TextField();
        field.setValue(m.content());
        field.setWidthFull();

        Button save = new Button("Save", ev -> {
            if (!field.getValue().isBlank()) {
                messageService.editMessage(conversation.id(), m.id(), field.getValue());
                refreshMessages();
                dialog.close();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancel = new Button("Cancel", ev -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout buttons = new HorizontalLayout(save, cancel);
        dialog.add(field, buttons);
        dialog.open();
    }

    private String initials(String name) {
        String[] parts = name.split(" ");
        if (parts.length >= 2) return "" + parts[0].charAt(0) + parts[1].charAt(0);
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
