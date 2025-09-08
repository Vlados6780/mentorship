package org.example.mentorship.service;

import org.example.mentorship.dto.MentorAllDataDto;
import org.example.mentorship.dto.MentorSearchRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GuestMentorshipService {

    ResponseEntity<List<MentorAllDataDto>> getAllMentors();
    ResponseEntity<List<MentorAllDataDto>> searchMentors(MentorSearchRequest searchRequest);

}
