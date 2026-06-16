package com.evolt.chatapp.repositories;

import com.evolt.chatapp.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
    SELECT n FROM Notification n 
    WHERE n.recipient.id = :userId 
    ORDER BY n.timestamp DESC
""")
    List<Notification> findByUserId(Long userId);

    @Query("""
    SELECT n FROM Notification n
    JOIN FETCH n.recipient
    WHERE n.recipient.id = :userId
      AND n.status = com.evolt.chatapp.models.enums.NotificationStatus.PENDING
    ORDER BY n.timestamp ASC
""")
    List<Notification> findPendingFromUserId(@Param("userId") Long userId);

    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.status = com.evolt.chatapp.models.enums.NotificationStatus.DELIVERED
    WHERE n.recipient.id = :userId AND n.status = com.evolt.chatapp.models.enums.NotificationStatus.PENDING
""")
    void markAllAsDelivered(Long userId);
}