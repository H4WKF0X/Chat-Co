package com.chatco.chatco.view.components;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.service.ConversationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.time.format.DateTimeFormatter;

/**
 * Modal dialog showing a user's profile card.
 *
 * <p>Displays avatar, name, username, email, presence status, and join date.
 * The "Send Message" button navigates to an existing direct message conversation
 * with the user, if one exists. Creating a new DM is not yet supported here.
 */
public class UserDetailDialog extends Dialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd. MMM yyyy");

    public UserDetailDialog(AppUser user, ConversationService conversationService) {
        addClassName("cc-user-detail-dialog");
        setWidth("320px");

        Avatar avatar = new Avatar(user.displayName());
        avatar.setWidth("64px");
        avatar.setHeight("64px");
        avatar.addClassName("cc-user-detail-avatar");

        Span name = new Span(user.displayName());
        name.addClassName("cc-user-detail-name");

        Span username = new Span("@" + user.username());
        username.addClassName("cc-user-detail-username");

        Span email = new Span(user.mail());
        email.addClassName("cc-user-detail-email");

        String statusLabel = switch (user.status()) {
            case ACTIVE   -> "Active";
            case AWAY     -> "Away";
            case INACTIVE -> "Inactive";
        };
        Span statusDot = new Span();
        statusDot.addClassName("cc-status-dot");
        statusDot.addClassName("cc-status-dot--" + user.status().name().toLowerCase());
        Span statusText = new Span(statusLabel);
        statusText.addClassName("cc-user-detail-status");
        Div statusRow = new Div(statusDot, statusText);
        statusRow.addClassName("cc-user-detail-status-row");

        Span since = new Span("Member since " + user.createdAt().format(DATE_FMT));
        since.addClassName("cc-user-detail-since");

        Button dmBtn = new Button("Send Message");
        dmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        dmBtn.addClickListener(e -> {
            close();
            conversationService.getByType(ConversationType.DIRECT).stream()
                    .filter(c -> {
                        var members = conversationService.getMembers(c.id());
                        return members.stream().anyMatch(m -> m.id().equals(user.id()));
                    })
                    .findFirst()
                    .ifPresent(c -> UI.getCurrent().navigate("conversation/" + c.id()));
        });

        Button closeBtn = new Button("Close");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        closeBtn.addClickListener(e -> close());

        Div actions = new Div(dmBtn, closeBtn);
        actions.addClassName("cc-user-detail-actions");

        Div body = new Div(avatar, name, username, email, statusRow, since, actions);
        body.addClassName("cc-user-detail-body");
        add(body);
    }
}
