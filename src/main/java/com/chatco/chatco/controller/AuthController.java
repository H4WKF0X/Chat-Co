package com.chatco.chatco.controller;

import com.chatco.chatco.dto.LoginRequest;
import com.chatco.chatco.dto.LoginResponse;
import com.chatco.chatco.security.LdapAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LdapAuthService ldapAuthService;

    public AuthController(LdapAuthService ldapAuthService) {
        this.ldapAuthService = ldapAuthService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                request.username(),
                request.password()
        );

        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültige Anmeldedaten");
        }

        return new LoginResponse(
                true,
                request.username(),
                (String) profile.get("displayName"),
                (String) profile.get("mail")
        );
    }
}