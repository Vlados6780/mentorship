package org.example.mentorship.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MentorSearchRequest {
    private String query;
    private String specialization;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private Integer minExperience;
    private BigDecimal minRating;
    private String sortBy = "averageRating";
    private String sortDirection = "DESC";
}