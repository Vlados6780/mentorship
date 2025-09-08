package org.example.mentorship.repository;


import org.example.mentorship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Modifying
    @Query("DELETE FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffTime")
    int deleteByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime cutoffTime);
}
