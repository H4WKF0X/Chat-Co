package com.chatco.chatco.service.stub;

import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.UserRole;
import com.chatco.chatco.model.UserStatus;
import com.chatco.chatco.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
        return findById(store.MAX.id()).orElse(store.MAX);
    }

    @Override
    public void updateUser(AppUser updated) {
        synchronized (store.allUsers) {
            for (int i = 0; i < store.allUsers.size(); i++) {
                if (store.allUsers.get(i).id().equals(updated.id())) {
                    store.allUsers.set(i, updated);
                    return;
                }
            }
        }
    }

    @Override
    public void addUser(String username, String displayName, String mail, UserRole role) {
        long newId = store.getUserIdSeq().incrementAndGet();
        store.allUsers.add(new AppUser(newId, username, displayName, mail, true, UserStatus.ACTIVE, role, OffsetDateTime.now()));
    }
}