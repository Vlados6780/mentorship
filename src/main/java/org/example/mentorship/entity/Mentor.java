package org.example.mentorship.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Mentor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mentor")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    private User user;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "average_rating")
    private BigDecimal averageRating;

    @Column(name = "mentor_target_students")
    private String mentorTargetStudents;

    @Column(name = "subscription_active")
    private boolean subscriptionActive = false;

    @Column(name = "subscription_expiry_date")
    private LocalDateTime subscriptionExpiryDate;
}
