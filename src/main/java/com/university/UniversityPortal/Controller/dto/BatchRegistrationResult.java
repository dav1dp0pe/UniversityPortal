package com.university.UniversityPortal.Controller.dto;

public record BatchRegistrationResult(
        Long offeringId,
        boolean success,
        String message
) {}
