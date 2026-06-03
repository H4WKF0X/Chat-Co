package com.chatco.chatco.web;

import java.util.Locale;

/**
 * Identifies which kind of client called the REST API.
 *
 * <p>Controllers can accept this enum as a method argument because
 * {@link ClientTypeArgumentResolver} reads it from the {@code X-Client-Type}
 * request header.</p>
 */
public enum ClientType {
    WEB,
    IOS,
    UNKNOWN;

    public static final String HEADER_NAME = "X-Client-Type";

    /**
     * Converts a header value into a known client type.
     *
     * @param value raw {@code X-Client-Type} header value
     * @return matching client type, or {@link #UNKNOWN} if missing or invalid
     */
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
