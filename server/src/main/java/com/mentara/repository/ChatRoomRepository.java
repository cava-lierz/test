package com.mentara.repository;

import com.mentara.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByIsDeletedFalse();
    Optional<ChatRoom> findByIdAndIsDeletedFalse(Long id);
} 