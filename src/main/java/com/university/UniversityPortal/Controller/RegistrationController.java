package com.university.UniversityPortal.Controller;

import com.university.UniversityPortal.Controller.dto.BatchRegistrationResult;
import com.university.UniversityPortal.Controller.dto.DropRequest;
import com.university.UniversityPortal.Controller.dto.EnrollmentResponse;
import com.university.UniversityPortal.Controller.dto.RegisterRequest;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    //dependency injection
    //private final EnrollmentRepository enrollmentRepository;
    private final RegistrationService registrationService;

/*
    //get mapping for registration page
    @GetMapping("/students/{studentId}/enrollments")
    public String getRegistrationPage() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        //model.addAttribute("enrollments", enrollments);
        return "registration";
    }
*/
    //post mapping for registering for classes
    //@PostMapping("/students/{studentId}/enrollments")
    @PostMapping
    public ResponseEntity<EnrollmentResponse> register(@RequestBody RegisterRequest request) {
        Enrollment enrollment = registrationService.registerForClass(request.studentId(), request.offeringId());
        // Implementation for registering a student for a class
        return ResponseEntity.ok(new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentStatus().name(),        //TODO: check if name() is appropriate here, or if we need id instead
                enrollment.getStudent().getStudentId(),
                enrollment.getCourseOffering().getOfferingId()));
    }

    //post mapping for dropping classes
    @PostMapping("/drop")
    public ResponseEntity<EnrollmentResponse> drop(@RequestBody DropRequest request) {
        Enrollment enrollment = registrationService.dropClass(request.studentId(), request.offeringId());
        // Implementation for dropping a class
        return ResponseEntity.ok(new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentStatus().name(),        //TODO: check if name() is appropriate here, or if we need id instead
                enrollment.getStudent().getStudentId(),
                enrollment.getCourseOffering().getOfferingId()));
    }

    //post mapping to add wishlist classes
    @PostMapping("/wishlist/add")
    public Wishlist addToWishlist(@RequestBody RegisterRequest request) {
        return registrationService.addToWishlist(request.studentId(), request.offeringId());
    }

    //post mapping to remove wishlist classes
    @PostMapping("/wishlist/remove")
    public void removeFromWishlist(@RequestBody RegisterRequest request) {
        registrationService.removeFromWishlist(request.studentId(), request.offeringId());
    }

    @PostMapping("/wishlist/register-all")
    public List<BatchRegistrationResult> registerAll(@RequestParam Long studentId, @RequestParam String term) {
        return registrationService.registerAllFromWishlist(studentId, term);
    }


}
