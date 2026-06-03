package com.chatco.chatco.service.db;

import com.chatco.chatco.model.Room;
import com.chatco.chatco.repository.RoomRepository;
import com.chatco.chatco.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DbRoomService implements RoomService {

    private final RoomRepository roomRepo;

    public DbRoomService(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    @Override
    public List<Room> getAll() {
        return roomRepo.findAll().stream().map(this::toRecord).toList();
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepo.findById(id).map(this::toRecord);
    }

    Room toRecord(com.chatco.chatco.entity.Room entity) {
        return new Room(entity.getId(), entity.getName(), entity.getCapacity(), entity.getLocation());
    }
}
