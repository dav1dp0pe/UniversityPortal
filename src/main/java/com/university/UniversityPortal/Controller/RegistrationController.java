package com.university.UniversityPortal.Controller;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Repository.EnrollmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class RegistrationController {

    //dependency injection
    private final EnrollmentRepository enrollmentRepository;

    public RegistrationController(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    //get mapping for registration page
    @GetMapping("/students/{studentId}/enrollments")
    public String getRegistrationPage() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        //model.addAttribute("enrollments", enrollments);
        return "registration";
    }

    //post mapping for registering for classes
    @PostMapping("/students/{studentId}/enrollments")
    public String registerForClass() {
        // Implementation for registering a student for a class
        return "redirect:/students/{studentId}/enrollments";
    }
}
