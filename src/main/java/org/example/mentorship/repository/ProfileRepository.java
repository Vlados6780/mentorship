package org.example.mentorship.repository;

import org.example.mentorship.entity.Profile;
import org.example.mentorship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    Optional<Profile> findByUserId(Integer userId);
    Optional<Profile> findByUser(User user);
    boolean existsByUserId(Integer userId);
}
