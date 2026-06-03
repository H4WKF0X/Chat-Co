package com.chatco.chatco.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A conversation between one or more users — either a channel, a direct message, or a group.
 *
 * <p>Meetings also own a linked group conversation created at scheduling time
 * so participants can chat in context of the meeting.
 */
public record Conversation(
        Long id,
        ConversationType type,
        String title,
        AppUser creator,
        OffsetDateTime createdAt
) {
    public String displayTitle(AppUser currentUser, List<AppUser> members) {
        if (type != ConversationType.DIRECT) {
            return safeTitle(title, "Untitled");
        }

        return members.stream()
                .filter(member -> currentUser == null || !member.id().equals(currentUser.id()))
                .findFirst()
                .map(AppUser::displayName)
                .orElseGet(() -> {
                    if (currentUser != null && currentUser.displayName().equals(title)) {
                        return "Direct Message";
                    }
                    return safeTitle(title, "Direct Message");
                });
    }

    private static String safeTitle(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
