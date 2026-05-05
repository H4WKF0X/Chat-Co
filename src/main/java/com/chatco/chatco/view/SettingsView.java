package com.chatco.chatco.view;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.UserRole;
import com.chatco.chatco.model.UserStatus;
import com.chatco.chatco.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * User settings view with four tabs: Profile, Appearance, Notifications, and Account.
 *
 * <p>Profile changes (display name, status) and appearance settings (theme, accent
 * colour) are applied immediately to the current session. Password change and account
 * deactivation are stubbed and show a notification until the backend is connected.
 */
@Route(value = "settings", layout = MainLayout.class)
@AnonymousAllowed
public class SettingsView extends VerticalLayout {

    public SettingsView(UserService userService) {
        addClassName("cc-settings-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        AppUser user = userService.getCurrentUser();

        Div header = new Div(new Span("Settings"));
        header.addClassName("cc-settings-header");

        Tab profileTab      = new Tab("Profile");
        Tab appearanceTab   = new Tab("Appearance");
        Tab notifTab        = new Tab("Notifications");
        Tab accountTab      = new Tab("Account");
        Tabs tabs = new Tabs(profileTab, appearanceTab, notifTab, accountTab);
        tabs.addClassName("cc-settings-tabs");

        Div content = new Div();
        content.addClassName("cc-settings-content");

        Div profilePanel     = buildProfilePanel(user, userService);
        Div appearancePanel  = buildAppearancePanel();
        Div notifPanel       = buildNotificationsPanel();
        Div accountPanel     = buildAccountPanel(user);

        content.add(profilePanel, appearancePanel, notifPanel, accountPanel);

        appearancePanel.setVisible(false);
        notifPanel.setVisible(false);
        accountPanel.setVisible(false);

        tabs.addSelectedChangeListener(e -> {
            Tab selected = e.getSelectedTab();
            profilePanel.setVisible(selected == profileTab);
            appearancePanel.setVisible(selected == appearanceTab);
            notifPanel.setVisible(selected == notifTab);
            accountPanel.setVisible(selected == accountTab);
        });

        add(header, tabs, content);
        expand(content);
    }

    private Div buildProfilePanel(AppUser user, UserService userService) {
        Div panel = new Div();
        panel.addClassName("cc-settings-panel");

        Avatar avatar = new Avatar(user.displayName());
        avatar.setWidth("72px");
        avatar.setHeight("72px");

        Button changePhoto = new Button("Change photo");
        changePhoto.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        changePhoto.addClickListener(e -> toast("Photo upload not yet implemented", false));

        Div avatarRow = new Div(avatar, changePhoto);
        avatarRow.addClassName("cc-settings-avatar-row");

        TextField displayName = new TextField("Display name");
        displayName.setValue(user.displayName());
        displayName.setReadOnly(user.role() != UserRole.ADMINISTRATOR);
        displayName.setWidthFull();

        TextField username = new TextField("Username");
        username.setValue(user.username());
        username.setReadOnly(true);
        username.setWidthFull();

        TextField email = new TextField("Email");
        email.setValue(user.mail());
        email.setReadOnly(true);
        email.setWidthFull();

        ComboBox<UserStatus> statusBox = new ComboBox<>("Status");
        statusBox.setItems(UserStatus.values());
        statusBox.setValue(user.status());
        statusBox.setItemLabelGenerator(s -> switch (s) {
            case ACTIVE   -> "Active";
            case AWAY     -> "Away";
            case INACTIVE -> "Inactive";
        });
        statusBox.setWidthFull();

        Button save = new Button("Save changes");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            userService.updateUser(new AppUser(
                    user.id(), user.username(), displayName.getValue().trim(), user.mail(),
                    user.active(), statusBox.getValue(), user.role(), user.createdAt()));
            toast("Profile saved", true);
        });

        panel.add(avatarRow, displayName, username, email, statusBox, save);
        return panel;
    }

    private Div buildAppearancePanel() {
        Div panel = new Div();
        panel.addClassName("cc-settings-panel");

        Span themeLabel = new Span("Theme");
        themeLabel.addClassName("cc-settings-label");

        Button darkBtn  = new Button("Dark");
        Button lightBtn = new Button("Light");
        darkBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        lightBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        UI.getCurrent().getPage().executeJs("return localStorage.getItem('cc-theme')")
                .then(String.class, saved -> {
                    if ("light".equals(saved)) {
                        lightBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    } else {
                        darkBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    }
                });

        darkBtn.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                    "document.documentElement.setAttribute('theme','dark');" +
                    "localStorage.setItem('cc-theme','dark')");
            darkBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            lightBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });
        lightBtn.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs(
                    "document.documentElement.removeAttribute('theme');" +
                    "localStorage.setItem('cc-theme','light')");
            lightBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            darkBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        HorizontalLayout themeRow = new HorizontalLayout(darkBtn, lightBtn);
        themeRow.addClassName("cc-settings-theme-row");

        Span accentLabel = new Span("Accent colour");
        accentLabel.addClassName("cc-settings-label");

        // Each entry: accent, hover, subtle, 50pct, 10pct, CSS class
        String[][] accents = {
            {"#5b8dee", "#4a7de0", "rgba(91,141,238,0.14)",  "rgba(91,141,238,0.5)",   "rgba(91,141,238,0.1)",   "cc-swatch--blue"},
            {"#3ba55c", "#2d9050", "rgba(59,165,92,0.14)",   "rgba(59,165,92,0.5)",    "rgba(59,165,92,0.1)",    "cc-swatch--green"},
            {"#ed4245", "#d93235", "rgba(237,66,69,0.14)",   "rgba(237,66,69,0.5)",    "rgba(237,66,69,0.1)",    "cc-swatch--red"},
            {"#faa61a", "#e5960a", "rgba(250,166,26,0.14)",  "rgba(250,166,26,0.5)",   "rgba(250,166,26,0.1)",   "cc-swatch--yellow"},
            {"#9b59b6", "#8949a6", "rgba(155,89,182,0.14)",  "rgba(155,89,182,0.5)",   "rgba(155,89,182,0.1)",   "cc-swatch--purple"},
            {"#1abc9c", "#0aa98b", "rgba(26,188,156,0.14)",  "rgba(26,188,156,0.5)",   "rgba(26,188,156,0.1)",   "cc-swatch--teal"}
        };
        Div swatches = new Div();
        swatches.addClassName("cc-settings-swatches");
        for (String[] accent : accents) {
            String color = accent[0], hover = accent[1], subtle = accent[2],
                   pct50 = accent[3], pct10 = accent[4], cls = accent[5];
            Div swatch = new Div();
            swatch.addClassName("cc-settings-swatch");
            swatch.addClassName(cls);
            swatch.getElement().setAttribute("tabindex", "0");
            swatch.getElement().setAttribute("role", "button");
            swatch.getElement().setAttribute("aria-label", "Set accent colour to " + cls.replace("cc-swatch--", ""));
            Runnable applyAccent = () -> UI.getCurrent().getPage().executeJs(
                    "var r=document.documentElement;" +
                    "r.style.setProperty('--cc-accent',$0);" +
                    "r.style.setProperty('--cc-accent-hover',$1);" +
                    "r.style.setProperty('--cc-accent-subtle',$2);" +
                    "r.style.setProperty('--lumo-primary-color-50pct',$3);" +
                    "r.style.setProperty('--lumo-primary-color-10pct',$4);" +
                    "localStorage.setItem('cc-accent',$0);",
                    color, hover, subtle, pct50, pct10);
            swatch.addClickListener(e -> applyAccent.run());
            swatch.getElement().addEventListener("keydown", e -> applyAccent.run())
                    .setFilter("event.key === 'Enter' || event.key === ' '");
            swatches.add(swatch);
        }

        panel.add(themeLabel, themeRow, accentLabel, swatches);
        return panel;
    }

    private Div buildNotificationsPanel() {
        Div panel = new Div();
        panel.addClassName("cc-settings-panel");

        panel.add(buildToggleRow("Sound on new message", false));
        panel.add(buildToggleRow("Desktop notifications", false));
        return panel;
    }

    private Div buildToggleRow(String label, boolean defaultOn) {
        Checkbox toggle = new Checkbox(label, defaultOn);
        toggle.addValueChangeListener(e -> toast(label + ": " + (e.getValue() ? "on" : "off") + " (stub)", false));
        Div row = new Div(toggle);
        row.addClassName("cc-settings-toggle-row");
        return row;
    }

    private Div buildAccountPanel(AppUser user) {
        Div panel = new Div();
        panel.addClassName("cc-settings-panel");

        Button changePassword = new Button("Change Password");
        changePassword.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        changePassword.addClickListener(e -> openChangePasswordDialog());
        panel.add(changePassword);

        if (user.role() == UserRole.ADMINISTRATOR) {
            Button deactivate = new Button("Deactivate Account");
            deactivate.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deactivate.addClickListener(e -> openDeactivateDialog());
            panel.add(deactivate);
        }

        return panel;
    }

    private void openChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Change Password");

        PasswordField current = new PasswordField("Current password");
        PasswordField newPass = new PasswordField("New password");
        PasswordField confirm = new PasswordField("Confirm new password");
        current.setWidthFull();
        newPass.setWidthFull();
        confirm.setWidthFull();

        Button save = new Button("Save", e -> {
            toast("Password change not yet implemented", false);
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        dialog.add(current, newPass, confirm, new HorizontalLayout(save, cancel));
        dialog.open();
    }

    private void openDeactivateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Deactivate Account");

        Span msg = new Span("Are you sure you want to deactivate your account? This cannot be undone.");
        msg.addClassName("cc-confirm-msg");

        Button confirm = new Button("Deactivate", e -> {
            toast("Account deactivation not yet implemented", false);
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        dialog.add(msg, new HorizontalLayout(confirm, cancel));
        dialog.open();
    }

    private void toast(String message, boolean success) {
        Notification notif = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        if (success) notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
