package com.chatco.chatco.repository;

import com.chatco.chatco.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Database access for physical meeting rooms.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByName(String name);
    boolean existsByName(String name);
}
