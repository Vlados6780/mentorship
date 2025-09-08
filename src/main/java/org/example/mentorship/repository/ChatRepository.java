package org.example.mentorship.repository;

import org.example.mentorship.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    List<Chat> findByStudentId(Integer studentId);
    List<Chat> findByMentorId(Integer mentorId);
    Optional<Chat> findByStudentIdAndMentorId(Integer studentId, Integer mentorId);
}
