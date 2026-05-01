package com.chatco.chatco.view;

import com.chatco.chatco.model.*;
import com.chatco.chatco.service.ConversationService;
import com.chatco.chatco.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

/**
 * Admin dashboard, accessible only to users with the {@code ADMINISTRATOR} role.
 *
 * <p>Provides three tabs: user management, channel management, and system info.
 * Destructive actions (delete, deactivate) and data exports are stubbed and
 * show a notification until the backend is connected.
 */
@Route(value = "admin", layout = MainLayout.class)
@AnonymousAllowed
public class AdminView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserService userService;
    private final ConversationService conversationService;

    public AdminView(UserService userService, ConversationService conversationService) {
        this.userService = userService;
        this.conversationService = conversationService;

        addClassName("cc-admin-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (userService.getCurrentUser().role() != UserRole.ADMINISTRATOR) {
            event.forwardTo(EmptyView.class);
            return;
        }
        buildContent();
    }

    private void buildContent() {
        removeAll();

        Div pageHeader = new Div(new Span("Admin Dashboard"));
        pageHeader.addClassName("cc-page-header");
        ((Span) pageHeader.getChildren().findFirst().get()).addClassName("cc-page-title");

        Tab usersTab    = new Tab("Users");
        Tab channelsTab = new Tab("Channels");
        Tab systemTab   = new Tab("System");
        Tabs tabs = new Tabs(usersTab, channelsTab, systemTab);
        tabs.addClassName("cc-settings-tabs");

        Div usersPanel    = buildUsersPanel();
        Div channelsPanel = buildChannelsPanel();
        Div systemPanel   = buildSystemPanel();

        channelsPanel.setVisible(false);
        systemPanel.setVisible(false);

        Div content = new Div(usersPanel, channelsPanel, systemPanel);
        content.addClassName("cc-admin-content");

        tabs.addSelectedChangeListener(e -> {
            Tab sel = e.getSelectedTab();
            usersPanel.setVisible(sel == usersTab);
            channelsPanel.setVisible(sel == channelsTab);
            systemPanel.setVisible(sel == systemTab);
        });

        add(pageHeader, tabs, content);
        expand(content);
    }

    private Div buildUsersPanel() {
        Div panel = new Div();
        panel.addClassName("cc-admin-panel");

        Button exportBtn = new Button("Export CSV");
        exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        exportBtn.addClickListener(e -> toast("CSV export not yet implemented", false));

        Button addUserBtn = new Button("Add User");
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addUserBtn.addClickListener(e -> openAddUserDialog());

        Div toolbar = new Div(exportBtn, addUserBtn);
        toolbar.addClassName("cc-admin-toolbar");

        Grid<AppUser> grid = new Grid<>(AppUser.class, false);
        grid.addColumn(AppUser::username).setHeader("Username").setSortable(true).setFlexGrow(2);
        grid.addColumn(AppUser::displayName).setHeader("Display Name").setSortable(true).setFlexGrow(2);
        grid.addColumn(AppUser::mail).setHeader("Email").setFlexGrow(3);
        grid.addColumn(u -> u.role().name()).setHeader("Role").setSortable(true).setFlexGrow(1);
        grid.addColumn(u -> u.status().name()).setHeader("Status").setSortable(true).setFlexGrow(1);
        grid.addColumn(u -> u.createdAt().format(DATE_FMT)).setHeader("Created").setFlexGrow(1);
        grid.addComponentColumn(u -> buildUserActions(u, grid)).setHeader("Actions").setFlexGrow(0).setWidth("160px");

        grid.setItems(userService.getAll());
        grid.addClassName("cc-admin-grid");
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        panel.add(toolbar, grid);
        return panel;
    }

    private Div buildUserActions(AppUser user, Grid<AppUser> grid) {
        Button toggle = new Button(user.active() ? "Deactivate" : "Activate");
        toggle.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                user.active() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);
        toggle.addClickListener(e -> toast((user.active() ? "Deactivated" : "Activated") + " " + user.displayName() + " (stub)", false));

        Button edit = new Button("Edit");
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        edit.addClickListener(e -> openEditUserDialog(user));

        Div actions = new Div(toggle, edit);
        actions.addClassName("cc-admin-row-actions");
        return actions;
    }

    private void openAddUserDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add User");

        com.vaadin.flow.component.textfield.TextField username = new com.vaadin.flow.component.textfield.TextField("Username");
        com.vaadin.flow.component.textfield.TextField displayName = new com.vaadin.flow.component.textfield.TextField("Display Name");
        com.vaadin.flow.component.textfield.TextField email = new com.vaadin.flow.component.textfield.TextField("Email");
        ComboBox<UserRole> role = new ComboBox<>("Role");
        role.setItems(UserRole.values());
        role.setValue(UserRole.MITARBEITER);
        username.setWidthFull();
        displayName.setWidthFull();
        email.setWidthFull();
        role.setWidthFull();

        Button save = new Button("Add User", e -> {
            toast("User added (stub)", true);
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        dialog.add(username, displayName, email, role, new HorizontalLayout(save, cancel));
        dialog.open();
    }

    private void openEditUserDialog(AppUser user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit: " + user.displayName());

        ComboBox<UserRole> role = new ComboBox<>("Role");
        role.setItems(UserRole.values());
        role.setValue(user.role());
        role.setWidthFull();

        ComboBox<UserStatus> status = new ComboBox<>("Status");
        status.setItems(UserStatus.values());
        status.setValue(user.status());
        status.setWidthFull();

        Button save = new Button("Save", e -> {
            toast("Changes saved (stub)", true);
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        dialog.add(role, status, new HorizontalLayout(save, cancel));
        dialog.open();
    }

    private Div buildChannelsPanel() {
        Div panel = new Div();
        panel.addClassName("cc-admin-panel");

        Button newChannelBtn = new Button("New Channel");
        newChannelBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        newChannelBtn.addClickListener(e -> UI.getCurrent().navigate("new-channel"));

        Div toolbar = new Div(newChannelBtn);
        toolbar.addClassName("cc-admin-toolbar");

        Grid<Conversation> grid = new Grid<>(Conversation.class, false);
        grid.addColumn(Conversation::title).setHeader("Name").setSortable(true).setFlexGrow(3);
        grid.addColumn(c -> c.type().name()).setHeader("Type").setSortable(true).setFlexGrow(1);
        grid.addColumn(c -> c.creator().displayName()).setHeader("Created by").setFlexGrow(2);
        grid.addColumn(c -> c.createdAt().format(DATE_FMT)).setHeader("Created").setFlexGrow(1);
        grid.addComponentColumn(this::buildChannelActions).setHeader("Actions").setFlexGrow(0).setWidth("160px");

        grid.setItems(conversationService.getAll());
        grid.addClassName("cc-admin-grid");
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        panel.add(toolbar, grid);
        return panel;
    }

    private Div buildChannelActions(Conversation conv) {
        Button archive = new Button("Archive");
        archive.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        archive.addClickListener(e -> toast("Archived " + conv.title() + " (stub)", false));

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> openDeleteConvDialog(conv));

        Div actions = new Div(archive, delete);
        actions.addClassName("cc-admin-row-actions");
        return actions;
    }

    private void openDeleteConvDialog(Conversation conv) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete channel?");
        Span msg = new Span("Delete \"" + conv.title() + "\"? This cannot be undone.");
        msg.addClassName("cc-confirm-msg");

        Button confirm = new Button("Delete", e -> {
            toast("Deleted " + conv.title() + " (stub)", false);
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        dialog.add(msg, new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private Div buildSystemPanel() {
        Div panel = new Div();
        panel.addClassName("cc-admin-panel");

        Span logsLabel = new Span("System Logs");
        logsLabel.addClassName("cc-admin-section-label");

        Div logsBox = new Div(new Span("[INFO]  2026-04-22 09:00:01  Application started"),
                              new Span("[INFO]  2026-04-22 09:00:03  Database connection established"),
                              new Span("[INFO]  2026-04-22 09:01:12  User max.paesold logged in"),
                              new Span("[WARN]  2026-04-22 09:15:44  LDAP sync skipped — no backend connected"),
                              new Span("[INFO]  2026-04-22 10:02:05  Message sent in conversation #1"));
        logsBox.addClassName("cc-admin-logs");

        Span backupLabel = new Span("Backup");
        backupLabel.addClassName("cc-admin-section-label");

        Span lastBackup = new Span("Last backup: not yet connected");
        lastBackup.addClassName("cc-admin-last-backup");

        Button createBackup = new Button("Create Backup");
        createBackup.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBackup.addClickListener(e -> toast("Backup scheduled (stub)", true));

        panel.add(logsLabel, logsBox, backupLabel, lastBackup, createBackup);
        return panel;
    }

    private void toast(String message, boolean success) {
        Notification notif = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        if (success) notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
