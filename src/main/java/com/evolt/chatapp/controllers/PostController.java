package com.evolt.chatapp.controllers;

import com.evolt.chatapp.models.Post;
import com.evolt.chatapp.models.dto.CommentRequest;
import com.evolt.chatapp.models.dto.PostCommentDto;
import com.evolt.chatapp.models.dto.PostDto;
import com.evolt.chatapp.models.dto.UpdatePostRequest;
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

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        Long requesterId = currentUserId(request);
        return ResponseEntity.ok(postService.getUserPosts(userId, requesterId, page, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestParam String content,
            @RequestParam(defaultValue = "PUBLIC") String privacy,
            @RequestParam(required = false) MultipartFile image,
            HttpServletRequest request
    ) {
        Long authorId = currentUserId(request);
        try {
            PostDto dto = postService.createPost(authorId, content, privacy, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest body,
            HttpServletRequest request
    ) {
        Long requesterId = currentUserId(request);
        try {
            return ResponseEntity.ok(postService.updatePost(id, requesterId, body));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long requesterId = currentUserId(request);
        try {
            postService.deletePost(id, requesterId);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getPostImage(@PathVariable Long id, HttpServletRequest request) {
        Long requesterId = currentUserId(request);

        Post post;
        try {
            post = postService.findByIdCheckingVisibility(id, requesterId);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        if (post.getImageUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = fileStorageLocation.resolve(post.getImageUrl()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = contentType != null
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok().contentType(mediaType).body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        try {
            postService.likePost(id, userId);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        postService.unlikePost(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id, HttpServletRequest request) {
        Long requesterId = currentUserId(request);
        try {
            List<PostCommentDto> comments = postService.getComments(id, requesterId);
            return ResponseEntity.ok(comments);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest body,
            HttpServletRequest request
    ) {
        Long authorId = currentUserId(request);
        try {
            PostCommentDto dto = postService.addComment(id, authorId, body.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        Long requesterId = currentUserId(request);
        try {
            postService.deleteComment(commentId, requesterId);
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