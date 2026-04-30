package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Dev-profile stub implementation of {@link UserService}.
 * Always returns the seeded admin user as the current user.
 * Swap for a real Spring Security–backed implementation when auth is wired up.
 */
@Service
@Profile("dev")
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