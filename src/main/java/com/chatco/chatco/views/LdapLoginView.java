package com.chatco.chatco.views;

import com.chatco.chatco.security.LdapAuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Map;

@Route("ldap-login")
public class LdapLoginView extends VerticalLayout {

    public LdapLoginView(LdapAuthService ldapAuthService) {

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        Pre output = new Pre(); // zeigt Text schön formatiert

        Button login = new Button("Login", e -> {
            Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                    username.getValue(),
                    password.getValue()
            );

            if (profile != null) {
                Notification.show("✅ Login erfolgreich");
                output.setText(profile.toString());
            } else {
                Notification.show("❌ Login fehlgeschlagen");
                output.setText("");
            }
        });

        add(username, password, login, output);
    }
}
