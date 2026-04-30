package com.chatco.chatco.model;

import java.time.OffsetDateTime;

/**
 * A registered user account.
 *
 * <p>{@code active} is {@code false} for deactivated accounts; deactivated
 * users cannot log in but their data is retained for message history.
 */
public record AppUser(
        Long id,
        String username,
        String displayName,
        String mail,
        boolean active,
        UserStatus status,
        UserRole role,
        OffsetDateTime createdAt
) {}