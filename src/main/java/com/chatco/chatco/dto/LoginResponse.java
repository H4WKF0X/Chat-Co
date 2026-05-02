package com.chatco.chatco.dto;

public record LoginResponse(
        boolean success,
        String username,
        String displayName,
        String mail,
        String token        // ← NEU: JWT-Token für die iOS App
) {}