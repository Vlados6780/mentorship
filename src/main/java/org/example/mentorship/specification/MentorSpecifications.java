package org.example.mentorship.specification;

import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.Profile;
import org.example.mentorship.entity.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import java.math.BigDecimal;

public class MentorSpecifications {

    // Returns a specification to filter mentors by specialization
    public static Specification<Mentor> hasSpecialization(String specialization) {
        return (root, query, criteriaBuilder) -> {
            if (specialization == null || specialization.isEmpty()) {
                return criteriaBuilder.conjunction(); // Returns true if no specialization is provided
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("specialization")),
                    "%" + specialization.toLowerCase() + "%"); // Case-insensitive partial match
        };
    }

    // Returns a specification to filter mentors by minimum rating
    public static Specification<Mentor> hasMinRating(BigDecimal minRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null) {
                return criteriaBuilder.conjunction(); // Returns true if no minimum rating is provided
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating);
        };
    }

    // Returns a specification to filter mentors by price range (hourly rate)
    public static Specification<Mentor> hasPriceRange(BigDecimal minRate, BigDecimal maxRate) {
        return (root, query, criteriaBuilder) -> {
            if (minRate == null && maxRate == null) {
                return criteriaBuilder.conjunction(); // Returns true if no range is provided
            }

            if (minRate == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("hourlyRate"), maxRate);
            }

            if (maxRate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("hourlyRate"), minRate);
            }

            return criteriaBuilder.between(root.get("hourlyRate"), minRate, maxRate);
        };
    }

    // Returns a specification to filter mentors by minimum years of experience
    public static Specification<Mentor> hasMinExperience(Integer minExperience) {
        return (root, query, criteriaBuilder) -> {
            if (minExperience == null) {
                return criteriaBuilder.conjunction(); // Returns true if no minimum experience is provided
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("experienceYears"), minExperience);
        };
    }

    // Returns a specification to filter mentors by a general search query
    public static Specification<Mentor> matchesGeneralQuery(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (query == null || query.isEmpty()) {
                return criteriaBuilder.conjunction(); // Returns true if no query is provided
            }

            String likePattern = "%" + query.toLowerCase() + "%"; // Case-insensitive search pattern

            // Join with User and Profile entities for additional fields
            Join<Mentor, User> userJoin = root.join("user");
            Join<User, Profile> profileJoin = userJoin.join("profile");

            // Match query against multiple fields (specialization, first name, last name, bio, target students)
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("specialization")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(profileJoin.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(profileJoin.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(profileJoin.get("bio")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("mentorTargetStudents")), likePattern)
            );
        };
    }
}
