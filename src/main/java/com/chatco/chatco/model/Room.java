package com.chatco.chatco.model;

/** A bookable physical meeting room. */
public record Room(
        Long id,
        String name,
        int capacity,
        String location
) {}
