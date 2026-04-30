package com.chatco.chatco.model;

import java.time.OffsetDateTime;

public record FileAttachment(
        Long id,
        String originalName,
        String storedName,
        String mimeType,
        long sizeBytes,
        String storagePath,
        OffsetDateTime uploadedAt,
        AppUser uploadedBy
) {}