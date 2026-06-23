package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findAllByConnected(boolean connected);

    @Modifying
    @Transactional
    @Query("""
        UPDATE User u
        SET u.connected = :connected
        WHERE u.username = :username
    """)
    int setConnected(
            @Param("connected") boolean connected,
            @Param("username") String username
    );

    User findByEmail(String email);
}

