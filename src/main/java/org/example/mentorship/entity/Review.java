package org.example.mentorship.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_review")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_student", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mentor", nullable = false)
    private Mentor mentor;

    @Column(name = "review_comment", nullable = false)
    private String comment;

    @Column(name = "review_rating", nullable = false)
    private Integer rating;

    @Column(name = "review_created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
