package com.chatco.chatco.view;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Theme(value = "chat-co", variant = Lumo.DARK)
@Meta(name = "color-scheme", content = "dark")
public class AppConfig implements AppShellConfigurator {}