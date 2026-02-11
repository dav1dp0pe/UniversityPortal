package com.university.UniversityPortal.Controller.dto;

public record RegistrationResponse(
        boolean success,
        String message,
        EnrollmentResponse enrollment // null if failed
) {
}
