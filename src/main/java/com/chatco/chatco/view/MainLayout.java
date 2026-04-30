package com.chatco.chatco.view;

import com.chatco.chatco.model.UserRole;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import com.chatco.chatco.view.components.SidebarComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Application shell shared by all routes.
 *
 * <p>Renders the narrow icon rail on the left, the conversation sidebar, and
 * the main content area. Implements {@link AfterNavigationObserver} to keep
 * the active rail icon and sidebar state in sync with the current route.
 * The admin rail icon is hidden for non-administrator users.
 */
@AnonymousAllowed
public class MainLayout extends HorizontalLayout implements RouterLayout, AfterNavigationObserver {

    private final SidebarComponent sidebar;
    private final Div contentArea = new Div();

    private final Div railChat;
    private final Div railMeetings;
    private final Div railAdmin;

    public MainLayout(ConversationService conversationService, UserService userService) {
        addClassName("cc-app-shell");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        sidebar = new SidebarComponent(conversationService, userService);
        sidebar.setOnConversationSelect(conv ->
                UI.getCurrent().navigate("conversation/" + conv.id())
        );

        contentArea.addClassName("cc-content");
        contentArea.setSizeFull();

        boolean isAdmin = userService.getCurrentUser().role() == UserRole.ADMINISTRATOR;
        railChat     = buildRailIcon("💬", "Chat",     () -> UI.getCurrent().navigate(""));
        railMeetings = buildRailIcon("📅", "Meetings", () -> UI.getCurrent().navigate("meetings"));
        railAdmin    = buildRailIcon("⚙", "Admin",    () -> UI.getCurrent().navigate("admin"));
        railAdmin.setVisible(isAdmin);

        add(buildRail(isAdmin), sidebar, contentArea);
        expand(contentArea);
    }

    private Div buildRail(boolean isAdmin) {
        Div rail = new Div();
        rail.addClassName("cc-rail");

        Image logo = new Image("images/logo.png", "Chat-Co");
        logo.addClassName("cc-rail-logo");

        Div navIcons = new Div(railChat, railMeetings);
        if (isAdmin) navIcons.add(railAdmin);
        navIcons.addClassName("cc-rail-nav");

        Div settings = buildRailIcon("⚙️", "Settings", () -> UI.getCurrent().navigate("settings"));
        settings.addClassName("cc-rail-settings");

        rail.add(logo, navIcons, settings);
        return rail;
    }

    private Div buildRailIcon(String icon, String tooltip, Runnable onClick) {
        Div btn = new Div();
        btn.addClassName("cc-rail-btn");
        btn.getElement().setAttribute("title", tooltip);
        Span iconSpan = new Span(icon);
        btn.add(iconSpan);
        btn.addClickListener(e -> onClick.run());
        return btn;
    }

    @Override
    public void showRouterLayoutContent(com.vaadin.flow.component.HasElement content) {
        contentArea.getElement().removeAllChildren();
        if (content != null) {
            contentArea.getElement().appendChild(content.getElement());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String path = event.getLocation().getPath();

        railChat.removeClassName("cc-rail-btn--active");
        railMeetings.removeClassName("cc-rail-btn--active");
        railAdmin.removeClassName("cc-rail-btn--active");

        if (path.startsWith("conversation/") || path.isEmpty()) {
            railChat.addClassName("cc-rail-btn--active");
        } else if (path.startsWith("meetings")) {
            railMeetings.addClassName("cc-rail-btn--active");
        } else if (path.startsWith("admin")) {
            railAdmin.addClassName("cc-rail-btn--active");
        }

        boolean showSidebar = !path.startsWith("meetings") && !path.startsWith("admin");
        sidebar.setVisible(showSidebar);

        if (path.startsWith("conversation/")) {
            try {
                Long id = Long.parseLong(path.substring("conversation/".length()));
                sidebar.setActiveConversation(id);
            } catch (NumberFormatException ignored) {
                sidebar.setActiveConversation(null);
            }
        } else {
            sidebar.setActiveConversation(null);
        }
    }
}
