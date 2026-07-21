package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.PostAttachment;
import com.evolt.chatapp.models.dto.PostCommentDto;
import com.evolt.chatapp.models.dto.PostDto;
import com.evolt.chatapp.models.dto.UpdatePostRequest;
import com.evolt.chatapp.services.PostCommentService;
import com.evolt.chatapp.services.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostCommentService commentService;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public PostController(PostService postService, PostCommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(postService.getUserPosts(userId, currentUserId(request), page, size));
    }

    /** Ranked, friends-first global feed. */
    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(postService.getFeed(currentUserId(request), page, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestParam String content,
            @RequestParam(defaultValue = "PUBLIC") String privacy,
            @RequestParam(required = false) List<MultipartFile> media,
            HttpServletRequest request
    ) {
        try {
            PostDto dto = postService.createPost(currentUserId(request), content, privacy, media);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody UpdatePostRequest body, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(postService.updatePost(id, currentUserId(request), body));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            postService.deletePost(id, currentUserId(request));
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Resource> getPostMedia(@PathVariable Long id, @PathVariable Long mediaId, HttpServletRequest request) {
        PostAttachment attachment;
        try {
            attachment = postService.findMediaCheckingVisibility(id, mediaId, currentUserId(request));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = fileStorageLocation.resolve(attachment.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();

            String contentType = attachment.getFileType() != null ? attachment.getFileType() : Files.probeContentType(filePath);
            MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok().contentType(mediaType).body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Comments ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Long requesterId = currentUserId(request);
        try {
            postService.findByIdCheckingVisibility(id, requesterId);
            return ResponseEntity.ok(commentService.getTopLevelComments(id, requesterId, page, size));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<Page<PostCommentDto>> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(commentService.getReplies(commentId, currentUserId(request), page, size));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        String content = (String) body.get("content");
        Long parentCommentId = body.get("parentCommentId") != null ? Long.valueOf(body.get("parentCommentId").toString()) : null;
        try {
            PostCommentDto dto = commentService.addComment(id, currentUserId(request), content, parentCommentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        try {
            commentService.deleteComment(commentId, currentUserId(request));
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Long currentUserId(HttpServletRequest request) {
        return Long.parseLong(request.getAttribute("userId").toString());
    }
}