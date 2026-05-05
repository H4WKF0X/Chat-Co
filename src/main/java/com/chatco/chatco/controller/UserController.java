// controller/UserController.java
package com.chatco.chatco.controller;

import com.chatco.chatco.repository.AppUserRepository;
import com.chatco.chatco.dto.UserResponse;
import com.chatco.chatco.web.ClientType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // GET /api/users/search?q=...
    @GetMapping("/search")
    public List<UserResponse> search(@RequestParam String q, ClientType clientType) {
        return appUserRepository.searchByUsernameOrDisplayName(q)
                .stream()
                .map(u -> new UserResponse(
                        u.getUsername(),
                        u.getDisplayName(),
                        u.getMail()))
                .toList();
    }
}
