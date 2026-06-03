package com.chatco.chatco.model;

/**
 * Classifies a conversation by its structural type.
 *
 * <ul>
 *   <li>{@code CHANNEL} – named topic channel open to multiple members</li>
 *   <li>{@code DIRECT} – one-to-one direct message between two users</li>
 *   <li>{@code GROUP} – named group conversation; also used for meeting chats</li>
 * </ul>
 */
public enum ConversationType {
    CHANNEL, DIRECT, GROUP;

    public static ConversationType fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            return CHANNEL;
        }
        return ConversationType.valueOf(value.trim().toUpperCase());
    }
}
