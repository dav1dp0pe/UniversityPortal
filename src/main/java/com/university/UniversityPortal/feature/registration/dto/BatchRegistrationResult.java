package com.university.UniversityPortal.feature.registration.dto;

public record BatchRegistrationResult(
        Long offeringId,
        boolean success,
        String message
) {}
