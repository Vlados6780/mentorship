package org.example.mentorship.service;

import org.example.mentorship.dto.UpdateProfileRequest;
import org.example.mentorship.entity.Profile;
import org.example.mentorship.entity.User;
import org.example.mentorship.repository.MentorRepository;
import org.example.mentorship.repository.ProfileRepository;
import org.example.mentorship.repository.StudentRepository;
import org.example.mentorship.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultUserService implements UserService {

    private static final String ROLE_STUDENT = "ROLE_STUDENT";
    private static final String ROLE_MENTOR = "ROLE_MENTOR";

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final ProfileRepository profileRepository;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public DefaultUserService(StudentRepository studentRepository,
                              MentorRepository mentorRepository,
                              ProfileRepository profileRepository,
                              CurrentUserProvider currentUserProvider) {
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.profileRepository = profileRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public ResponseEntity<?> getCurrentUserProfile() {
        return currentUserProvider.withCurrentUser(user -> {
            try {
                Map<String, Object> response = new HashMap<>();
                Profile profile = profileRepository.findByUser(user).orElse(null);

                if (profile != null) {
                    response.put("firstName", profile.getFirstName());
                    response.put("lastName", profile.getLastName());
                    response.put("bio", profile.getBio());
                    response.put("age", profile.getAge());
                    response.put("profilePictureUrl", profile.getProfilePictureUrl());
                } else {
                    response.put("firstName", "User");
                }

                if (user.getRole() != null) {
                    if (ROLE_STUDENT.equals(user.getRole().getRoleName())) {
                        studentRepository.findByUser(user).ifPresent(student -> {
                            response.put("educationLevel", student.getEducationLevel());
                            response.put("learningGoals", student.getLearningGoals());
                        });
                    } else if (ROLE_MENTOR.equals(user.getRole().getRoleName())) {
                        mentorRepository.findByUser(user).ifPresent(mentor -> {
                            response.put("hourlyRate", mentor.getHourlyRate());
                            response.put("specialization", mentor.getSpecialization());
                            response.put("experienceYears", mentor.getExperienceYears());
                            response.put("averageRating", mentor.getAverageRating());
                            response.put("mentorTargetStudents", mentor.getMentorTargetStudents());
                        });
                    }
                }

                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateCurrentUserProfile(UpdateProfileRequest request) {
        return currentUserProvider.withCurrentUser(user -> {
            try {
                updateBasicProfile(user, request);

                String roleName = user.getRole().getRoleName();

                if (ROLE_STUDENT.equals(roleName)) {
                    updateStudentProfile(user, request);
                } else if (ROLE_MENTOR.equals(roleName)) {
                    updateMentorProfile(user, request);
                }

                return ResponseEntity.ok(Map.of("message", "Profile successfully updated"));

            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
            }
        });
    }

    private void updateBasicProfile(User user, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.firstName() != null) profile.setFirstName(request.firstName());
        if (request.lastName() != null) profile.setLastName(request.lastName());
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.age() != null) profile.setAge(request.age());

        profileRepository.save(profile);
    }

    private void updateStudentProfile(User user, UpdateProfileRequest request) {
        studentRepository.findByUser(user).ifPresent(student -> {
            if (request.educationLevel() != null)
                student.setEducationLevel(request.educationLevel());
            if (request.learningGoals() != null)
                student.setLearningGoals(request.learningGoals());

            studentRepository.save(student);
        });
    }

    private void updateMentorProfile(User user, UpdateProfileRequest request) {
        mentorRepository.findByUser(user).ifPresent(mentor -> {
            if (request.hourlyRate() != null)
                mentor.setHourlyRate(request.hourlyRate());
            if (request.specialization() != null)
                mentor.setSpecialization(request.specialization());
            if (request.experienceYears() != null)
                mentor.setExperienceYears(request.experienceYears());
            if (request.mentorTargetStudents() != null)
                mentor.setMentorTargetStudents(request.mentorTargetStudents());

            mentorRepository.save(mentor);
        });
    }

    @Override
    public ResponseEntity<?> getUserProfilePicture() {
        return currentUserProvider.withCurrentUser(user -> {
            try {
                Profile profile = profileRepository.findByUser(user)
                        .orElseThrow(() -> new RuntimeException("Profile not found"));

                String profilePictureUrl = profile.getProfilePictureUrl();
                if (profilePictureUrl == null || profilePictureUrl.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }

                Map<String, String> response = Map.of("profilePictureUrl", profilePictureUrl);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: " + e.getMessage());
            }
        });
    }
}
