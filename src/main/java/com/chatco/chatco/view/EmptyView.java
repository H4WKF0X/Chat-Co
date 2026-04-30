package com.chatco.chatco.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Default home view shown when no conversation is selected.
 *
 * <p>Also used as the redirect target when a requested route is inaccessible
 * (e.g. unknown conversation ID, insufficient role for admin view).
 */
@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class EmptyView extends Div {

    public EmptyView() {
        addClassName("cc-empty-state");
        setSizeFull();

        Span icon     = new Span("💬");
        Span title    = new Span("No conversation selected");
        Span subtitle = new Span("Pick a channel or direct message from the sidebar");

        icon.addClassName("cc-empty-icon");
        title.addClassName("cc-empty-title");
        subtitle.addClassName("cc-empty-subtitle");

        add(icon, title, subtitle);
    }
}