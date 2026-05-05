package com.chatco.chatco.web;

import java.util.Locale;

public enum ClientType {
    WEB,
    IOS,
    UNKNOWN;

    public static final String HEADER_NAME = "X-Client-Type";

    public static ClientType fromHeader(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "web", "website" -> WEB;
            case "ios", "iphone", "ipad" -> IOS;
            default -> UNKNOWN;
        };
    }

    public boolean isWeb() {
        return this == WEB;
    }

    public boolean isIos() {
        return this == IOS;
    }
}
