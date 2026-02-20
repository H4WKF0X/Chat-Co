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
public class File_attachment {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private App_user uploadedBy;
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;
    @NotNull
    @Column(name = "storage_path", nullable = false, length = Integer.MAX_VALUE)
    private String storagePath;
    @NotNull
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    @Size(max = 150)
    @NotNull
    @Column(name = "mime_type", nullable = false, length = 150)
    private String mimeType;
    @Size(max = 255)
    @NotNull
    @Column(name = "original_name", nullable = false)
    private String originalName;
    @Size(max = 255)
    @NotNull
    @Column(name = "stored_name", nullable = false)
    private String storedName;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
}
