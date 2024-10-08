package com.unitekndt.mqnavigator.repository;

import com.unitekndt.mqnavigator.entity.DM;
import com.unitekndt.mqnavigator.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DMRepository extends JpaRepository<DM, Long> {
    List<DM> findBySenderOrReceiver(User sender, User receiver);

    @Query("SELECT dm FROM DM dm WHERE dm.workspace.id = :workspaceId AND " +
            "((dm.sender.id = :currentUserId AND dm.receiver.id = :otherUserId) OR " +
            "(dm.sender.id = :otherUserId AND dm.receiver.id = :currentUserId)) " +
            "ORDER BY dm.createdAt DESC")
    List<DM> findDMsByUsers(
            @Param("workspaceId") Long workspaceId,
            @Param("otherUserId") Long otherUserId,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );


    @Query("SELECT COUNT(dm) FROM DM dm WHERE dm.workspace.url = :workspaceUrl " +
            "AND dm.sender.id = :senderId AND dm.receiver.id = :receiverId " +
            "AND dm.createdAt > :afterTimestamp")
    Long countUnreadDMs(
            @Param("workspaceId") String workspaceUrl,
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("afterTimestamp") LocalDateTime afterTimestamp
    );
}
