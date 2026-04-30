package com.chatco.chatco.view;

import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.service.ConversationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

/**
 * View for creating a new channel.
 *
 * <p>The channel name is lowercased and spaces are replaced with hyphens before
 * creation. The description and private-toggle fields are present in the UI but
 * not yet persisted by the stub backend.
 */
@Route(value = "new-channel", layout = MainLayout.class)
@AnonymousAllowed
public class NewChannelView extends VerticalLayout {

    public NewChannelView(ConversationService conversationService) {
        addClassName("cc-new-conv-view");
        setMaxWidth("480px");
        setPadding(false);

        Div header = new Div(new Span("Create a Channel"));
        header.addClassName("cc-new-conv-header");

        TextField nameField = new TextField("Channel name");
        nameField.setPlaceholder("e.g. marketing");
        nameField.setPrefixComponent(new Span("#"));
        nameField.setWidthFull();

        TextArea descField = new TextArea("Description (optional)");
        descField.setWidthFull();

        Checkbox privateToggle = new Checkbox("Private channel");

        Button create = new Button("Create Channel");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.addClickListener(e -> {
            String name = nameField.getValue().trim().toLowerCase().replace(" ", "-");
            if (!name.isBlank()) {
                var conv = conversationService.create(ConversationType.CHANNEL, name, List.of());
                UI.getCurrent().navigate("conversation/" + conv.id());
            } else {
                nameField.setInvalid(true);
                nameField.setErrorMessage("Channel name is required");
            }
        });

        Button cancel = new Button("Cancel", e -> UI.getCurrent().navigate(""));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Div actions = new Div(create, cancel);
        actions.addClassName("cc-new-conv-actions");

        add(header, nameField, descField, privateToggle, actions);
    }
}
