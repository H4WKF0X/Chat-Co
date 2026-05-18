// controller/MeetingController.java
package com.chatco.chatco.controller;

import com.chatco.chatco.dto.MeetingRequest;
import com.chatco.chatco.dto.MeetingResponse;
import com.chatco.chatco.entity.*;
import com.chatco.chatco.repository.*;
import com.chatco.chatco.web.ClientType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
/**
 * REST controller for meeting calendar operations.
 *
 * <p>Every meeting is backed by a group conversation so the meeting can later
 * have chat messages and participants connected to it.</p>
 */
public class MeetingController {

    private final MeetingRepository meetingRepository;
    private final AppUserRepository appUserRepository;
    private final ConversationRepository conversationRepository;

    public MeetingController(MeetingRepository meetingRepository,
                             AppUserRepository appUserRepository,
                             ConversationRepository conversationRepository) {
        this.meetingRepository = meetingRepository;
        this.appUserRepository = appUserRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * Returns meetings that start inside the given date-time range.
     *
     * <p>Endpoint: {@code GET /api/meetings?from=...&until=...}</p>
     */
    @GetMapping
    public List<MeetingResponse> getMeetings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime until,
            ClientType clientType) {

        return meetingRepository.findByStartAtBetween(from, until)
                .stream()
                .map(MeetingResponse::from)
                .toList();
    }

    /**
     * Creates a meeting and its backing group conversation.
     *
     * <p>The authenticated principal is expected to be the username that was
     * stored by {@link com.chatco.chatco.security.JwtFilter}.</p>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse createMeeting(
            @RequestBody MeetingRequest req,
            @AuthenticationPrincipal String username,
            ClientType clientType) {

        AppUser creator = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // Create the chat conversation first because the meeting has a required
        // one-to-one link to a conversation.
        Conversation conv = new Conversation();
        conv.setType("group");
        conv.setTitle(req.title());
        conv.setCreator(creator);
        conv = conversationRepository.save(conv);

        Meeting meeting = new Meeting();
        meeting.setTitle(req.title());
        meeting.setDescription(req.description());
        meeting.setStartAt(req.startAt());
        meeting.setEndAt(req.endAt());
        meeting.setLocationOrLink(req.locationOrLink());
        meeting.setConversation(conv);

        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    /**
     * Updates editable meeting fields.
     *
     * <p>Endpoint: {@code PUT /api/meetings/{id}}</p>
     */
    @PutMapping("/{id}")
    public MeetingResponse updateMeeting(@PathVariable Long id,
                                         @RequestBody MeetingRequest req,
                                         ClientType clientType) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        meeting.setTitle(req.title());
        meeting.setDescription(req.description());
        meeting.setStartAt(req.startAt());
        meeting.setEndAt(req.endAt());
        meeting.setLocationOrLink(req.locationOrLink());

        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    /**
     * Deletes a meeting by id.
     *
     * <p>Endpoint: {@code DELETE /api/meetings/{id}}</p>
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMeeting(@PathVariable Long id, ClientType clientType) {
        if (!meetingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        meetingRepository.deleteById(id);
    }
}
