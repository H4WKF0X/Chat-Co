package com.chatco.chatco.view;

import com.chatco.chatco.security.LdapAuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.List;
import java.util.Map;

@Route("ldap-login")
public class LdapLoginView extends VerticalLayout {

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public LdapLoginView(LdapAuthService ldapAuthService) {
        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button loginButton = new Button("Login", e -> {
            Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                    usernameField.getValue(),
                    passwordField.getValue()
            );

            if (profile != null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        usernameField.getValue(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

                HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
                HttpServletResponse response = VaadinServletResponse.getCurrent().getHttpServletResponse();
                securityContextRepository.saveContext(context, request, response);

                UI.getCurrent().navigate("");
            } else {
                Notification.show("Login fehlgeschlagen");
            }
        });

        add(usernameField, passwordField, loginButton);
    }
}
