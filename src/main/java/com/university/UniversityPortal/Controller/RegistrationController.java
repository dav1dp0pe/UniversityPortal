package com.university.UniversityPortal.Controller;

import com.university.UniversityPortal.Controller.dto.RegisterRequest;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Repository.EnrollmentRepository;
import com.university.UniversityPortal.Services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    //dependency injection
    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationService registrationService;


    //get mapping for registration page
    @GetMapping("/students/{studentId}/enrollments")
    public String getRegistrationPage() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        //model.addAttribute("enrollments", enrollments);
        return "registration";
    }

    //post mapping for registering for classes
    //@PostMapping("/students/{studentId}/enrollments")
    @PostMapping
    public ResponseEntity<Enrollment> register(@RequestBody RegisterRequest request) {
        Enrollment enrollment = registrationService.registerForClass(request.getStudentId(), request.getOfferingId());
        // Implementation for registering a student for a class
        return ResponseEntity.ok(enrollment);
    }
}
