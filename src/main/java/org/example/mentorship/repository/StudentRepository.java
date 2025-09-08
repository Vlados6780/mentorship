package org.example.mentorship.repository;

import org.example.mentorship.entity.Student;
import org.example.mentorship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByUser(User user);
    boolean existsByUserId(Integer userId);
}
