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

                // User anlegen
                AppUser user = new AppUser();
                user.setUsername(username);
                user.setDisplayName(displayName.isBlank() ? username : displayName);
                user.setMail(mail.isBlank() ? username + "@student.tgm.ac.at" : mail);
                user.setLdapUid(ldapUid.isBlank() ? username : ldapUid);
                user.setIsActive(true); // Falls dein Feld anders heißt -> anpassen

                AppUser savedUser = appUserRepository.save(user);

                // Rolle holen/erstellen
                Role role = roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            Role r = new Role();
                            r.setName(roleName);
                            return roleRepository.save(r);
                        });

                // user_role Verknüpfung
                UserRole userRole = new UserRole();
                userRole.setUser(savedUser);
                userRole.setRole(role);
                userRole.setId(new UserRoleId(savedUser.getId(), role.getId()));

                userRoleRepository.save(userRole);

                Notification.show("User + Rolle gespeichert ✅");

                // Felder leeren
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