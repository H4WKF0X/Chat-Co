package com.chatco.chatco.view;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.theme.Theme;

@Theme("chat-co")
@Meta(name = "color-scheme", content = "dark light")
public class AppConfig implements AppShellConfigurator {}