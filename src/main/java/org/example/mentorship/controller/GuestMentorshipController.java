package org.example.mentorship.controller;

import org.example.mentorship.dto.MentorAllDataDto;
import org.example.mentorship.dto.MentorSearchRequest;
import org.example.mentorship.service.GuestMentorshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/guest")
public class GuestMentorshipController {

    private final GuestMentorshipService guestMentorshipService;

    @Autowired
    public GuestMentorshipController(GuestMentorshipService guestMentorshipService) {
        this.guestMentorshipService = guestMentorshipService;
    }


    @GetMapping("/mentors")
    public CompletableFuture<ResponseEntity<List<MentorAllDataDto>>> getAllMentors() {
        return CompletableFuture.supplyAsync(
                guestMentorshipService::getAllMentors);
    }

    @PostMapping("/mentors/search")
    public CompletableFuture<ResponseEntity<List<MentorAllDataDto>>> searchMentors(
            @RequestBody MentorSearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(
                () -> guestMentorshipService.searchMentors(searchRequest));
    }

}
