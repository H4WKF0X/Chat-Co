// dto/UserResponse.java
package com.chatco.chatco.dto;

/**
 * Lightweight user data returned by user search endpoints.
 */
public record UserResponse(String id, String displayName, String email) {}
