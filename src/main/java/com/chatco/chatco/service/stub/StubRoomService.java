package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.Room;
import com.chatco.chatco.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StubRoomService implements RoomService {

    private final StubDataStore store;

    public StubRoomService(StubDataStore store) {
        this.store = store;
    }

    @Override
    public List<Room> getAll() {
        return store.allRooms;
    }

    @Override
    public Optional<Room> findById(Long id) {
        return store.allRooms.stream().filter(r -> r.id().equals(id)).findFirst();
    }
}
