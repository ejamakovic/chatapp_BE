package com.evolt.chatapp.services;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.models.Message;
import com.evolt.chatapp.repositories.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment saveAttachment(
            MultipartFile file,
            Message message
    ) throws IOException {

        String fileName = UUID.randomUUID()
                + "_" +
                file.getOriginalFilename();

        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        Attachment attachment = new Attachment();

        attachment.setFileUrl("/uploads/" + fileName);
        attachment.setFileType(file.getContentType());
        attachment.setMessage(message);

        return attachmentRepository.save(attachment);
    }
}
