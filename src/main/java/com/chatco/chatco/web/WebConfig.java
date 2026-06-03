package com.chatco.chatco.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
/**
 * Registers custom Spring MVC configuration for the REST layer.
 */
public class WebConfig implements WebMvcConfigurer {

    private final ClientTypeArgumentResolver clientTypeArgumentResolver;

    public WebConfig(ClientTypeArgumentResolver clientTypeArgumentResolver) {
        this.clientTypeArgumentResolver = clientTypeArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // Allows controller methods to declare a ClientType parameter directly.
        resolvers.add(clientTypeArgumentResolver);
    }
}
