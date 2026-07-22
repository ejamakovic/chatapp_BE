package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.repositories.AttachmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Transactional
    public Attachment saveAttachment(
            MultipartFile file,
            Message message
    ) throws IOException {

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new RuntimeException("Invalid file");
        }

        if (
                !contentType.startsWith("image/") &&
                        !contentType.startsWith("video/")
        ) {
            throw new RuntimeException("Unsupported file type");
        }

        String originalName = file.getOriginalFilename();
        String fileName =
                UUID.randomUUID() + "_" + originalName;
        Path uploadDir = Paths.get("uploads");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path path = uploadDir.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                path,
                StandardCopyOption.REPLACE_EXISTING
        );

        Attachment attachment = new Attachment();
        attachment.setFileUrl("/uploads/" + fileName);
        attachment.setFileType(contentType);
        attachment.setMessage(message);

        return attachmentRepository.save(attachment);
    }

    public List<Attachment> findByConversationId(Long id) {
        return attachmentRepository.findByConversationId(id);
    }
}
