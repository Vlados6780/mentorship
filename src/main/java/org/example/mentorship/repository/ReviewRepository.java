package org.example.mentorship.repository;

import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.Review;
import org.example.mentorship.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByMentorOrderByCreatedAtDesc(Mentor mentor);

    Optional<Review> findByStudentAndMentor(Student student, Mentor mentor);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentor = :mentor")
    Double calculateAverageRatingForMentor(@Param("mentor") Mentor mentor);

}
