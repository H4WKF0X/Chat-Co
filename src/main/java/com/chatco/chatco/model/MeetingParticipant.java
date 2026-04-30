package com.chatco.chatco.model;

/** Join record linking a user to a meeting with their current RSVP status. */
public record MeetingParticipant(
        Meeting meeting,
        AppUser user,
        ParticipantStatus status
) {}