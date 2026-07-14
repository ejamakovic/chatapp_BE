package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}