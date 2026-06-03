package com.chatco.chatco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "file_attachment")
/**
 * Metadata for a file stored outside the database.
 */
public class FileAttachment {
    /** User who uploaded the file. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private AppUser uploadedBy;

    /** Upload timestamp. */
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    /** Absolute or application-relative path where the file content is stored. */
    @NotNull
    @Column(name = "storage_path", nullable = false, length = Integer.MAX_VALUE)
    private String storagePath;

    /** File size in bytes. */
    @NotNull
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /** MIME type supplied or detected during upload. */
    @Size(max = 150)
    @NotNull
    @Column(name = "mime_type", nullable = false, length = 150)
    private String mimeType;

    /** Original filename from the user's device. */
    @Size(max = 255)
    @NotNull
    @Column(name = "original_name", nullable = false)
    private String originalName;

    /** Filename used in storage, normally unique to avoid collisions. */
    @Size(max = 255)
    @NotNull
    @Column(name = "stored_name", nullable = false)
    private String storedName;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
}
