package com.chatco.chatco.view;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

/**
 * View for starting a new direct message conversation.
 *
 * <p>Shows a searchable list of all users except the current one. Clicking a
 * user opens the existing DM if one already exists, otherwise creates a new one.
 */
@Route(value = "new-dm", layout = MainLayout.class)
@AnonymousAllowed
public class NewDmView extends VerticalLayout {

    public NewDmView(UserService userService, ConversationService conversationService) {
        addClassName("cc-new-conv-view");
        setSizeFull();
        setPadding(false);

        Div header = new Div(new Span("New Direct Message"));
        header.addClassName("cc-new-conv-header");

        TextField search = new TextField();
        search.setPlaceholder("Search for a user...");
        search.setWidthFull();
        search.addClassName("cc-new-conv-search");

        AppUser currentUser = userService.getCurrentUser();
        List<AppUser> others = userService.getAll().stream()
                .filter(u -> !u.id().equals(currentUser.id()) && u.active())
                .toList();

        Div list = new Div();
        list.addClassName("cc-new-conv-list");

        search.addValueChangeListener(e -> {
            list.removeAll();
            String q = e.getValue().toLowerCase();
            others.stream()
                    .filter(u -> u.displayName().toLowerCase().contains(q) || u.username().toLowerCase().contains(q))
                    .forEach(u -> list.add(buildUserRow(u, conversationService, currentUser)));
        });

        others.forEach(u -> list.add(buildUserRow(u, conversationService, currentUser)));

        add(header, search, list);
    }

    private Div buildUserRow(AppUser user, ConversationService conversationService, AppUser currentUser) {
        Avatar avatar = new Avatar(user.displayName());
        avatar.setWidth("36px");
        avatar.setHeight("36px");

        Span name = new Span(user.displayName());
        name.addClassName("cc-new-conv-user-name");

        Span username = new Span("@" + user.username());
        username.addClassName("cc-new-conv-user-sub");

        Div info = new Div(name, username);
        info.addClassName("cc-new-conv-user-info");

        Span dot = new Span();
        dot.addClassName("cc-status-dot");
        dot.addClassName("cc-status-dot--" + user.status().name().toLowerCase());

        Div row = new Div(avatar, info, dot);
        row.addClassName("cc-new-conv-user-row");
        row.getElement().setAttribute("tabindex", "0");
        row.getElement().setAttribute("role", "button");
        Runnable openDm = () -> conversationService.getByType(ConversationType.DIRECT).stream()
                .filter(c -> {
                    var members = conversationService.getMembers(c.id());
                    return members.stream().anyMatch(m -> m.id().equals(user.id()))
                            && members.stream().anyMatch(m -> m.id().equals(currentUser.id()));
                })
                .findFirst()
                .ifPresentOrElse(
                        c -> UI.getCurrent().navigate("conversation/" + c.id()),
                        () -> {
                            var conv = conversationService.create(ConversationType.DIRECT, user.displayName(), List.of(user.id()));
                            UI.getCurrent().navigate("conversation/" + conv.id());
                        }
                );
        row.addClickListener(e -> openDm.run());
        row.getElement().addEventListener("keydown", e -> openDm.run())
                .setFilter("event.key === 'Enter' || event.key === ' '");
        return row;
    }
}
