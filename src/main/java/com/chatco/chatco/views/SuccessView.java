package com.chatco.chatco.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("success")
/**
 * Minimal page shown after the Vaadin LDAP login succeeds.
 */
public class SuccessView extends VerticalLayout {
    public SuccessView() {
        add(new H1("✅ Eingeloggt"));
    }
}
