package com.chatco.chatco.views;

import com.chatco.chatco.security.LdapAuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Pre;
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

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        Pre output = new Pre(); // zeigt Text schön formatiert

        Button login = new Button("Login", e -> {
            Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                    username.getValue(),
                    password.getValue()
            );

            if (profile != null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username.getValue(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

                HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
                HttpServletResponse response = VaadinServletResponse.getCurrent().getHttpServletResponse();
                securityContextRepository.saveContext(context, request, response);

                Notification.show("Login erfolgreich");
                output.setText(profile.toString());
                UI.getCurrent().navigate("success");
            } else {
                Notification.show("Login fehlgeschlagen");
                output.setText("");
            }
        });

        add(username, password, login, output);
    }
}
