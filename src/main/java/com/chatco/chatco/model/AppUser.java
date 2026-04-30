package com.chatco.chatco.model;

import java.time.OffsetDateTime;

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