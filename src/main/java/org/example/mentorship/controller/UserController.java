package org.example.mentorship.controller;

import org.example.mentorship.dto.UpdateProfileRequest;
import org.example.mentorship.service.ProfileService;
import org.example.mentorship.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AsyncTaskExecutor executor;
    private final UserService userService;
    private final ProfileService profileService;

    @Autowired
    public UserController(
            @Qualifier("securityAwareAsyncExecutor") AsyncTaskExecutor executor,
            UserService userService,
            ProfileService profileService) {
        this.executor = executor;
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public CompletableFuture<ResponseEntity<?>> getUserProfile() {
        return CompletableFuture.supplyAsync(userService::getCurrentUserProfile, executor);
    }

    @PutMapping("/update-profile")
    public CompletableFuture<ResponseEntity<?>> updateUserProfile(@RequestBody UpdateProfileRequest request) {
        return CompletableFuture.supplyAsync(() -> userService.updateCurrentUserProfile(request), executor);
    }

    @GetMapping("/profile-picture")
    public CompletableFuture<ResponseEntity<?>> getUserProfilePicture() {
        return CompletableFuture.supplyAsync(userService::getUserProfilePicture, executor);
    }

    @PostMapping(value = "/update-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<?>> updateProfilePicture(
            @RequestPart("profilePicture") MultipartFile profilePicture) {
        return CompletableFuture.supplyAsync(
                () -> profileService.updateProfilePicture(profilePicture),
                executor
        );
    }
}
