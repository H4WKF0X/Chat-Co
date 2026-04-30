package com.chatco.chatco.service;

import com.chatco.chatco.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<AppUser> getAll();
    Optional<AppUser> findById(Long id);
    AppUser getCurrentUser();
}