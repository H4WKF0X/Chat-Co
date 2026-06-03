package com.chatco.chatco.view;

import com.chatco.chatco.entity.AppUser;
import com.chatco.chatco.security.LdapAuthService;
import com.chatco.chatco.security.UserAuthorityService;
import com.chatco.chatco.security.UserProvisioningService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Map;
import java.util.Optional;

@Route("ldap-login")
@AnonymousAllowed
public class LdapLoginView extends VerticalLayout {

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public LdapLoginView(LdapAuthService ldapAuthService,
                         UserProvisioningService userProvisioningService,
                         UserAuthorityService userAuthorityService) {
        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");

        Button loginButton = new Button("Login", e -> {
            String username = usernameField.getValue();
            Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                    username,
                    passwordField.getValue()
            );

            if (profile != null) {
                String displayName = Optional.ofNullable(profile.get("displayName"))
                        .or(() -> Optional.ofNullable(profile.get("cn")))
                        .map(Object::toString)
                        .orElse(username);
                String mail = Optional.ofNullable(profile.get("mail"))
                        .map(Object::toString)
                        .orElse("");

                AppUser appUser = userProvisioningService.loadOrCreateFromLdap(
                        username, username, displayName, mail);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        appUser.getUsername(),
                        null,
                        userAuthorityService.authoritiesForUser(appUser)
                );

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

                HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
                HttpServletResponse response = VaadinServletResponse.getCurrent().getHttpServletResponse();
                securityContextRepository.saveContext(context, request, response);

                UI.getCurrent().getPage().setLocation("/");
            } else {
                Notification.show("Login fehlgeschlagen");
            }
        });

        add(usernameField, passwordField, loginButton);
    }
}
