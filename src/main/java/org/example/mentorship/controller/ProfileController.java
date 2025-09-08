package org.example.mentorship.controller;

import org.example.mentorship.dto.MentorInfoDto;
import org.example.mentorship.dto.ProfileDtoRequest;
import org.example.mentorship.dto.StudentInfoDto;
import org.example.mentorship.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CompletableFuture<ResponseEntity<?>> createProfile(
            @RequestParam("userId") Integer userId,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("bio") String bio,
            @RequestParam("age") Integer age,
            @RequestPart(value = "studentInfo", required = false) StudentInfoDto studentInfo,
            @RequestPart(value = "mentorInfo", required = false) MentorInfoDto mentorInfo,
            @RequestPart("profilePicture") MultipartFile profilePicture) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProfileDtoRequest profileDto = new ProfileDtoRequest(userId, firstName, lastName, bio, age);
                profileService.createProfile(profileDto, studentInfo, mentorInfo, profilePicture);
                return ResponseEntity.status(HttpStatus.CREATED).body("Profile successfully created");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating profile: " + e.getMessage());
            }
        });
    }
}
