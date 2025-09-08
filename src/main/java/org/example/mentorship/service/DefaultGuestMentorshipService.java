package org.example.mentorship.service;

import org.example.mentorship.dto.MentorAllDataDto;
import org.example.mentorship.dto.MentorSearchRequest;
import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.Profile;
import org.example.mentorship.entity.User;
import org.example.mentorship.repository.MentorRepository;
import org.example.mentorship.repository.ProfileRepository;
import org.example.mentorship.specification.MentorSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultGuestMentorshipService implements GuestMentorshipService {

    private final MentorRepository mentorRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public DefaultGuestMentorshipService(MentorRepository mentorRepository,
                                         ProfileRepository profileRepository) {
        this.mentorRepository = mentorRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<MentorAllDataDto>> getAllMentors() {
        List<Mentor> mentors = mentorRepository.findAll();
        return ResponseEntity.ok(convertToDtoList(mentors));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<MentorAllDataDto>> searchMentors(MentorSearchRequest searchRequest) {
        Specification<Mentor> spec = MentorSpecifications.matchesGeneralQuery(searchRequest.getQuery())
                .and(MentorSpecifications.hasSpecialization(searchRequest.getSpecialization()))
                .and(MentorSpecifications.hasPriceRange(searchRequest.getMinRate(), searchRequest.getMaxRate()))
                .and(MentorSpecifications.hasMinExperience(searchRequest.getMinExperience()))
                .and(MentorSpecifications.hasMinRating(searchRequest.getMinRating()));

        // Define sorting
        Sort sort = createSortFromRequest(searchRequest);

        // Perform search with specification and sorting
        List<Mentor> mentors = mentorRepository.findAll(spec, sort);

        return ResponseEntity.ok(convertToDtoList(mentors));
    }

    private Sort createSortFromRequest(MentorSearchRequest searchRequest) {
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "averageRating";
        String direction = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "DESC";

        return Sort.by(
                direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy);
    }

    private List<MentorAllDataDto> convertToDtoList(List<Mentor> mentors) {
        List<MentorAllDataDto> mentorDtos = new ArrayList<>();

        for (Mentor mentor : mentors) {
            User user = mentor.getUser();
            Profile profile = profileRepository.findByUser(user).orElse(null);

            if (profile != null) {
                MentorAllDataDto mentorDto = new MentorAllDataDto(
                        mentor.getId(),
                        profile.getProfilePictureUrl(),
                        profile.getFirstName(),
                        profile.getLastName(),
                        profile.getBio(),
                        profile.getAge(),
                        mentor.getHourlyRate(),
                        mentor.getSpecialization(),
                        mentor.getExperienceYears(),
                        mentor.getAverageRating(),
                        mentor.getMentorTargetStudents()
                );

                mentorDtos.add(mentorDto);
            }
        }

        return mentorDtos;
    }
}
