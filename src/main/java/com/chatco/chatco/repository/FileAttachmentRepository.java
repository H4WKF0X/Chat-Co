package com.chatco.chatco.repository;

import com.chatco.chatco.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {
    Optional<FileAttachment> findFileAttachmentByStoredName(String storedName);
    List<FileAttachment> findFileAttachmentByOriginalName(String originalName);
    List<FileAttachment> findFileAttachmentByMimeType(String mimeType);
    Optional<FileAttachment> findFileAttachmentByStoragePath(String storagePath);
    List<FileAttachment> findFileAttachmentByUploadedById(Long uploadedById);
    boolean existsFileAttachmentByStoragePath(String storagePath);
    boolean existsFileAttachmentByStoredName(String storedName);
    List<FileAttachment> findFileAttachmentBySizeBytes(Long sizeBytes);
}
