package com.chatco.chatco.model;

public record Room(
        Long id,
        String name,
        int capacity,
        String location
) {}