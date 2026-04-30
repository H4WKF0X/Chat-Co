package com.chatco.chatco.model;

public record MeetingParticipant(
        Meeting meeting,
        AppUser user,
        ParticipantStatus status
) {}