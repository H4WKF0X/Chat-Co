package com.chatco.chatco.model;

import java.time.OffsetDateTime;

/**
 * Metadata for a file uploaded within a conversation.
 *
 * <p>The file content itself is stored on disk or object storage under
 * {@code storagePath}; this record holds only the metadata needed to serve
 * and display the attachment.
 */
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