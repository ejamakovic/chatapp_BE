package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Attachment;
import com.evolt.chatapp.repositories.AttachmentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public AttachmentController(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id, HttpServletRequest request) {
        Long authenticatedUserId = Long.parseLong(request.getAttribute("userId").toString());

        // Validate membership via the secure query
        Attachment attachment = attachmentRepository.findByIdAndUserId(id, authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied or File Not Found"));

        try {
            // Resolve the file from disk using the stored fileUrl or filename
            // If fileUrl stores the full path, extract just the filename
            String filename = attachment.getFileUrl().substring(attachment.getFileUrl().lastIndexOf("/") + 1);
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}