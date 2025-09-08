package org.example.mentorship.dto;

import jakarta.validation.constraints.*;

public record ProfileDtoRequest(
        @NotNull(message = "User ID cannot be null")
        Integer userId,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Bio cannot be blank")
        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio,

        @NotNull(message = "Age cannot be null")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must not exceed 120")
        Integer age
) {}
