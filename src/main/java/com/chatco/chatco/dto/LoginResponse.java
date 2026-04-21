package com.chatco.chatco.dto;

public record LoginResponse(
        boolean success,
        String username,
        String displayName,
        String mail
) {
}