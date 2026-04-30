package com.chatco.chatco.service;

import com.chatco.chatco.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Service for reading bookable meeting rooms.
 *
 * <p>The current implementation is a stub backed by in-memory data.
 * Replace with a JPA-backed implementation when the Database branch is merged.
 */
public interface RoomService {

    /** Returns all available rooms. */
    List<Room> getAll();

    /**
     * Looks up a room by its primary key.
     *
     * @param id the room ID
     * @return the room, or empty if not found
     */
    Optional<Room> findById(Long id);
}