package com.chatco.chatco.view.components;

import com.chatco.chatco.model.Conversation;
import com.chatco.chatco.model.ConversationType;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Persistent left sidebar showing channels, groups, and direct messages.
 *
 * <p>Each section has an add-button that navigates to the corresponding
 * creation view. The active conversation is highlighted via
 * {@link #setActiveConversation(Long)}. The user footer at the bottom
 * navigates to the settings view on click.
 */
public class SidebarComponent extends VerticalLayout {

    private final Map<Long, Div> navItems = new HashMap<>();
    private Consumer<Conversation> onSelect;

    // Stub unread counts keyed by conversation ID (StubDataStore: general=1, dev-talk=2, dm-alex=4)
    private static final Map<Long, Integer> UNREAD = Map.of(
            1L, 3,
            2L, 1,
            4L, 2
    );

    public SidebarComponent(ConversationService conversationService, UserService userService) {
        addClassName("cc-sidebar");
        setSizeUndefined();
        setPadding(false);
        setSpacing(false);

        Div workspaceHeader = new Div();
        workspaceHeader.addClassName("cc-workspace-header");
        workspaceHeader.setText("Chat-Co");

        Div scrollArea = new Div();
        scrollArea.addClassName("cc-sidebar-scroll");
        scrollArea.add(
                buildSection("# Channels",       ConversationType.CHANNEL, conversationService, "new-channel"),
                buildSection("⊞ Groups",          ConversationType.GROUP,   conversationService, "new-group"),
                buildSection("Direct Messages",   ConversationType.DIRECT,  conversationService, "new-dm")
        );

        Div userFooter = buildUserFooter(userService);

        add(workspaceHeader, scrollArea, userFooter);
        expand(scrollArea);
    }

    private Div buildSection(String title, ConversationType type,
                             ConversationService conversationService, String addRoute) {
        Div section = new Div();
        section.addClassName("cc-sidebar-section");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("cc-sidebar-section-title-text");

        Span addBtn = new Span("+");
        addBtn.addClassName("cc-sidebar-add-btn");
        addBtn.getElement().setAttribute("title", "New");
        addBtn.getElement().setAttribute("role", "button");
        addBtn.getElement().setAttribute("tabindex", "0");
        addBtn.getElement().setAttribute("aria-label", "New");
        addBtn.addClickListener(e -> UI.getCurrent().navigate(addRoute));
        addBtn.getElement().addEventListener("keydown", e -> UI.getCurrent().navigate(addRoute))
                .setFilter("event.key === 'Enter' || event.key === ' '");

        Div header = new Div(titleSpan, addBtn);
        header.addClassName("cc-sidebar-section-title");
        section.add(header);

        for (Conversation conv : conversationService.getByType(type)) {
            Div item = buildNavItem(conv);
            navItems.put(conv.id(), item);
            section.add(item);
        }
        return section;
    }

    private Div buildNavItem(Conversation conv) {
        Div item = new Div();
        item.addClassName("cc-nav-item");

        String prefix = conv.type() == ConversationType.CHANNEL ? "# " : " ";
        Span label = new Span(prefix + conv.title());
        label.addClassName("cc-nav-item-label");

        item.add(label);

        int unread = UNREAD.getOrDefault(conv.id(), 0);
        if (unread > 0) {
            Span badge = new Span(String.valueOf(unread));
            badge.addClassName("cc-unread-badge");
            item.add(badge);
        }

        item.addClickListener(e -> {
            if (onSelect != null) onSelect.accept(conv);
        });
        return item;
    }

    private Div buildUserFooter(UserService userService) {
        var user = userService.getCurrentUser();
        Div footer = new Div();
        footer.addClassName("cc-user-footer");
        footer.addClickListener(e -> UI.getCurrent().navigate("settings"));
        footer.getElement().setAttribute("role", "button");
        footer.getElement().setAttribute("tabindex", "0");
        footer.getElement().addEventListener("keydown", e -> UI.getCurrent().navigate("settings"))
                .setFilter("event.key === 'Enter' || event.key === ' '");

        Avatar avatar = new Avatar(user.displayName());
        avatar.setWidth("32px");
        avatar.setHeight("32px");

        Div info = new Div();
        info.addClassName("cc-user-footer-info");

        Span name = new Span(user.displayName());
        name.addClassName("cc-user-name");

        String statusLabel = switch (user.status()) {
            case ACTIVE   -> "Active";
            case AWAY     -> "Away";
            case INACTIVE -> "Inactive";
        };
        Span statusDot = new Span();
        statusDot.addClassName("cc-status-dot");
        statusDot.addClassName("cc-status-dot--" + user.status().name().toLowerCase());

        Span status = new Span(statusLabel);
        status.addClassName("cc-user-status");

        Div statusRow = new Div(statusDot, status);
        statusRow.addClassName("cc-user-status-row");

        info.add(name, statusRow);
        footer.add(avatar, info);
        return footer;
    }

    public void setOnConversationSelect(Consumer<Conversation> handler) {
        this.onSelect = handler;
    }

    public void setActiveConversation(Long id) {
        navItems.values().forEach(div -> div.removeClassName("cc-nav-item--active"));
        if (id != null) {
            Div item = navItems.get(id);
            if (item != null) item.addClassName("cc-nav-item--active");
        }
    }
}
