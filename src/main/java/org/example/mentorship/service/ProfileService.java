package org.example.mentorship.service;

import org.example.mentorship.dto.MentorInfoDto;
import org.example.mentorship.dto.ProfileDtoRequest;
import org.example.mentorship.dto.StudentInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ProfileService {
    void createProfile(ProfileDtoRequest profileDto, StudentInfoDto studentInfo, MentorInfoDto mentorInfo, MultipartFile profilePicture) throws IOException;
    ResponseEntity<?> updateProfilePicture(MultipartFile profilePicture);

}
