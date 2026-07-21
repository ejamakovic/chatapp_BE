package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
    SELECT p FROM Post p
    WHERE p.author.id = :authorId
    AND (
        :requesterId IS NULL
        OR :requesterId = :authorId
        OR p.privacy = com.evolt.chatapp.models.enums.PostPrivacy.PUBLIC
        OR (
            p.privacy = com.evolt.chatapp.models.enums.PostPrivacy.FRIENDS
            AND EXISTS (
                SELECT f FROM Friendship f
                WHERE f.status = com.evolt.chatapp.models.enums.FriendshipStatus.ACCEPTED
                AND (
                    (f.requester.id = :authorId AND f.addressee.id = :requesterId)
                    OR
                    (f.requester.id = :requesterId AND f.addressee.id = :authorId)
                )
            )
        )
    )
""")
    Page<Post> findVisiblePostsByAuthor(
            @Param("authorId") Long authorId,
            @Param("requesterId") Long requesterId,
            Pageable pageable
    );

    /** Candidate pool for the global feed — final ranking/pagination happens in PostService. */
    @Query("""
    SELECT p FROM Post p
    WHERE p.createdAt >= :since
    AND (
        p.privacy = com.evolt.chatapp.models.enums.PostPrivacy.PUBLIC
        OR p.author.id = :requesterId
        OR (p.privacy = com.evolt.chatapp.models.enums.PostPrivacy.FRIENDS AND p.author.id IN :friendIds)
    )
    ORDER BY p.createdAt DESC
""")
    List<Post> findFeedCandidates(
            @Param("requesterId") Long requesterId,
            @Param("friendIds") List<Long> friendIds,
            @Param("since") LocalDateTime since
    );
}