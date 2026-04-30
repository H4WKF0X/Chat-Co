package com.chatco.chatco.service;

import com.chatco.chatco.model.AppUser;

import java.util.List;
import java.util.Optional;

/**
 * Service for reading and resolving user accounts.
 *
 * <p>The current implementation is a stub backed by in-memory data.
 * Replace with a JPA-backed implementation when the Database branch is merged.
 */
public interface UserService {

    /** Returns all registered users, including inactive accounts. */
    List<AppUser> getAll();

    /**
     * Looks up a user by their primary key.
     *
     * @param id the user ID
     * @return the user, or empty if not found
     */
    Optional<AppUser> findById(Long id);

    /**
     * Returns the currently authenticated user.
     *
     * <p>The stub always returns the seeded admin user. Replace with a real
     * Spring Security principal lookup when authentication is wired up.
     */
    AppUser getCurrentUser();
}
