package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
    SELECT f from Friendship f 
    WHERE f.status = 'ACCEPTED' AND (f.requester.id = :userId OR f.addressee.id = :userId)
""")
    List<Friendship> findAllFromUser(Long userId);


}
