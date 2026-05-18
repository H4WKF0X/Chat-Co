// controller/AuthController.java
package com.chatco.chatco.controller;

import com.chatco.chatco.dto.LoginRequest;
import com.chatco.chatco.dto.LoginResponse;
import com.chatco.chatco.security.JwtUtil;
import com.chatco.chatco.security.LdapAuthService;
import com.chatco.chatco.web.ClientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
/**
 * REST endpoints for authentication.
 *
 * <p>The API login uses LDAP credentials and returns a JWT token that mobile
 * or web API clients can send as a {@code Bearer} token on later requests.</p>
 */
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final LdapAuthService ldapAuthService;
    private final JwtUtil jwtUtil;

    public AuthController(LdapAuthService ldapAuthService, JwtUtil jwtUtil) {
        this.ldapAuthService = ldapAuthService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user against LDAP and returns profile data plus a JWT.
     *
     * @param request username and password supplied by the client
     * @param clientType client type resolved from the {@code X-Client-Type} header
     * @return login result with username, display name, e-mail, and JWT token
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, ClientType clientType) {
        log.debug("Login request from client type {}", clientType);

        Map<String, Object> profile = ldapAuthService.loginAndFetchProfile(
                request.username(), request.password()
        );

        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ungültige Anmeldedaten");
        }

        // LDAP attributes can be missing, so convert them only after a null check.
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
