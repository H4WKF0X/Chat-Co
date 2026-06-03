// controller/UserController.java
package com.chatco.chatco.controller;

import com.chatco.chatco.repository.AppUserRepository;
import com.chatco.chatco.dto.UserResponse;
import com.chatco.chatco.web.ClientType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
/**
 * User API endpoints used by clients that need to find existing ChatCo users.
 */
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Searches users by username or display name.
     *
     * <p>Endpoint: {@code GET /api/users/search?q=...}</p>
     *
     * @param q search text entered by the client
     * @param clientType client type resolved from the {@code X-Client-Type} header
     * @return lightweight user records for search results
     */
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
