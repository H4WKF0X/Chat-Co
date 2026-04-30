package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StubUserService implements UserService {

    private final StubDataStore store;

    public StubUserService(StubDataStore store) {
        this.store = store;
    }

    @Override
    public List<AppUser> getAll() {
        return store.allUsers;
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        return store.allUsers.stream().filter(u -> u.id().equals(id)).findFirst();
    }

    @Override
    public AppUser getCurrentUser() {
        return store.MAX;
    }
}