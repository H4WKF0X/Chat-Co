package com.chatco.chatco.views;

import com.chatco.chatco.entity.AppUser;
import com.chatco.chatco.entity.Role;
import com.chatco.chatco.entity.UserRole;
import com.chatco.chatco.entity.UserRoleId;
import com.chatco.chatco.repository.AppUserRepository;
import com.chatco.chatco.repository.RoleRepository;
import com.chatco.chatco.repository.UserRoleRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN")
@Route("test-user-create")
/**
 * Admin-only helper view for creating test users and assigning one role.
 *
 * <p>This is intended for development or administration, not for public user
 * self-registration.</p>
 */
public class TestUserCreateView extends VerticalLayout {

    public TestUserCreateView(AppUserRepository appUserRepository,
                              RoleRepository roleRepository,
                              UserRoleRepository userRoleRepository) {

        setSpacing(true);
        setPadding(true);

        TextField usernameField = new TextField("Username");
        TextField displayNameField = new TextField("Display Name");
        EmailField mailField = new EmailField("E-Mail");
        TextField ldapUidField = new TextField("LDAP UID");

        ComboBox<String> roleBox = new ComboBox<>("Rolle");
        roleBox.setItems("ADMIN", "EMPLOYEE", "GUEST");
        roleBox.setValue("EMPLOYEE");

        Button saveButton = new Button("User anlegen", event -> {
            try {
                String username = usernameField.getValue() != null ? usernameField.getValue().trim() : "";
                String displayName = displayNameField.getValue() != null ? displayNameField.getValue().trim() : "";
                String mail = mailField.getValue() != null ? mailField.getValue().trim() : "";
                String ldapUid = ldapUidField.getValue() != null ? ldapUidField.getValue().trim() : "";
                String roleName = roleBox.getValue();

                if (username.isBlank()) {
                    Notification.show("Username fehlt");
                    return;
                }

                if (appUserRepository.findByUsername(username).isPresent()) {
                    Notification.show("User existiert bereits");
                    return;
                }

                // Create the local user row.
                AppUser user = new AppUser();
                user.setUsername(username);
                user.setDisplayName(displayName.isBlank() ? username : displayName);
                user.setMail(mail.isBlank() ? username + "@student.tgm.ac.at" : mail);
                user.setLdapUid(ldapUid.isBlank() ? username : ldapUid);
                user.setIsActive(true);

                AppUser savedUser = appUserRepository.save(user);

                // Reuse the role if it exists, otherwise create it.
                Role role = roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            Role r = new Role();
                            r.setName(roleName);
                            return roleRepository.save(r);
                        });

                // Create the many-to-many user_role link with its composite key.
                UserRole userRole = new UserRole();
                userRole.setUser(savedUser);
                userRole.setRole(role);
                userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));

                userRoleRepository.save(userRole);

                Notification.show("User + Rolle gespeichert ✅");

                // Clear the form for the next test user.
                usernameField.clear();
                displayNameField.clear();
                mailField.clear();
                ldapUidField.clear();
                roleBox.setValue("EMPLOYEE");

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Fehler: " + ex.getMessage());
            }
        });

        add(usernameField, displayNameField, mailField, ldapUidField, roleBox, saveButton);
    }
}
