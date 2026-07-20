package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Friendship;
import com.evolt.chatapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
        SELECT DISTINCT f
        FROM Friendship f
        WHERE f.status = 'ACCEPTED'
        AND (f.requester.id = :userId OR f.addressee.id = :userId)
    """)
    Page<Friendship> findAllFriendsFromUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Friendship f
        WHERE f.status = com.evolt.chatapp.models.enums.FriendshipStatus.ACCEPTED
        AND (
            (f.requester.id = :userId1 AND f.addressee.id = :userId2)
            OR (f.requester.id = :userId2 AND f.addressee.id = :userId1)
        )
    """)
    boolean existsAcceptedFriendshipBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("""
    SELECT f
    FROM Friendship f
    WHERE (f.requester.id = :requesterId OR f.addressee.id = :requesterId)
      AND (f.requester.id = :addresseeId OR f.addressee.id = :addresseeId)
""")
    Optional<Friendship> findFriendship(Long requesterId, Long addresseeId);
}