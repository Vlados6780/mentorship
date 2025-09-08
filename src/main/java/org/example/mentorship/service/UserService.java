package org.example.mentorship.service;

import org.example.mentorship.dto.UpdateProfileRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> getCurrentUserProfile();
    ResponseEntity<?> updateCurrentUserProfile(UpdateProfileRequest request);
    ResponseEntity<?> getUserProfilePicture();
}
