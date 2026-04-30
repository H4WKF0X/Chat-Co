package com.chatco.chatco.view;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.*;

/**
 * View for creating a new group conversation.
 *
 * <p>The current user is always included as creator; the member list shows all
 * other users with checkboxes. Clicking a row toggles the checkbox.
 */
@Route(value = "new-group", layout = MainLayout.class)
@AnonymousAllowed
public class NewGroupView extends VerticalLayout {

    private final Set<Long> selectedIds = new HashSet<>();

    public NewGroupView(UserService userService, ConversationService conversationService) {
        addClassName("cc-new-conv-view");
        setMaxWidth("480px");
        setPadding(false);

        AppUser currentUser = userService.getCurrentUser();

        Div header = new Div(new Span("Create a Group"));
        header.addClassName("cc-new-conv-header");

        TextField nameField = new TextField("Group name");
        nameField.setWidthFull();

        Span membersLabel = new Span("Add Members");
        membersLabel.addClassName("cc-new-conv-section-label");

        Div memberList = new Div();
        memberList.addClassName("cc-new-conv-list");

        userService.getAll().stream()
                .filter(u -> !u.id().equals(currentUser.id()))
                .forEach(u -> memberList.add(buildMemberCheckRow(u)));

        Button create = new Button("Create Group");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.addClickListener(e -> {
            String name = nameField.getValue().trim();
            if (!name.isBlank()) {
                var conv = conversationService.create(ConversationType.GROUP, name, new ArrayList<>(selectedIds));
                UI.getCurrent().navigate("conversation/" + conv.id());
            } else {
                nameField.setInvalid(true);
                nameField.setErrorMessage("Group name is required");
            }
        });

        Button cancel = new Button("Cancel", e -> UI.getCurrent().navigate(""));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Div actions = new Div(create, cancel);
        actions.addClassName("cc-new-conv-actions");

        add(header, nameField, membersLabel, memberList, actions);
    }

    private Div buildMemberCheckRow(AppUser user) {
        Avatar avatar = new Avatar(user.displayName());
        avatar.setWidth("32px");
        avatar.setHeight("32px");

        Span name = new Span(user.displayName());
        name.addClassName("cc-new-conv-user-name");

        Checkbox check = new Checkbox();
        check.addValueChangeListener(e -> {
            if (e.getValue()) selectedIds.add(user.id());
            else              selectedIds.remove(user.id());
        });

        Div row = new Div(check, avatar, name);
        row.addClassName("cc-new-conv-user-row");
        row.addClickListener(e -> check.setValue(!check.getValue()));
        return row;
    }
}
