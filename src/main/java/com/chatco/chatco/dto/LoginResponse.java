package com.chatco.chatco.dto;

/**
 * Response returned after a successful API login.
 *
 * <p>The token is a JWT and must be sent as {@code Authorization: Bearer <token>}
 * on protected API requests.</p>
 */
public record LoginResponse(
        boolean success,
        String username,
        String displayName,
        String mail,
        String token
) {}
