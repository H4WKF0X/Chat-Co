// controller/AuthController.java
package com.chatco.chatco.controller;

import com.chatco.chatco.dto.LoginRequest;
import com.chatco.chatco.dto.LoginResponse;
import com.chatco.chatco.security.JwtUtil;
import com.chatco.chatco.security.LdapAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LdapAuthService ldapAuthService;
    private final JwtUtil jwtUtil;

    public AuthController(LdapAuthService ldapAuthService, JwtUtil jwtUtil) {
        this.ldapAuthService = ldapAuthService;
        this.jwtUtil = jwtUtil;
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                request.username(), request.password()
        );

        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültige Anmeldedaten");
        }

        // Sicher auf null prüfen ohne toString() auf null aufzurufen
        String displayName = Optional.ofNullable(profile.get("displayName"))
                .map(Object::toString)
                .orElse(request.username());

        String mail = Optional.ofNullable(profile.get("mail"))
                .map(Object::toString)
                .orElse("");

        String token = jwtUtil.generateToken(request.username());

        return new LoginResponse(true, request.username(), displayName, mail, token);
    }
}