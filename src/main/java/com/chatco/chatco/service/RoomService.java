package com.chatco.chatco.service;

import com.chatco.chatco.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<Room> getAll();
    Optional<Room> findById(Long id);
}