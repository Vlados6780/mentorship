package org.example.mentorship.repository;

import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Integer>, JpaSpecificationExecutor<Mentor> {
    Optional<Mentor> findByUser(User user);
    boolean existsByUserId(Integer userId);
}