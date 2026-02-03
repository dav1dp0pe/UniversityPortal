package com.university.UniversityPortal.Controller;

import com.university.UniversityPortal.Controller.dto.*;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOfferingSearchResult;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registration")
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

    //get mapping for a student looking up a course's offerings for that semester
    @GetMapping("/offerings")
    public List<CourseOfferingSearchResponse> searchCourseOfferings(
            @RequestParam String semester,
            @RequestParam String courseCode){

        List<CourseOfferingSearchResult> results = registrationService.searchOfferingsBySemesterAndCourseCode(semester, courseCode);
        return results.stream().map(this::toCourseOfferingSearchResponse).toList();

    }

    //post mapping for registering for classes
    //@PostMapping("/students/{studentId}/enrollments")
    @PostMapping("/register")
    public ResponseEntity<EnrollmentResponse> register(@RequestBody RegisterRequest request) {

        Enrollment enrollment = registrationService.registerForClass(request.studentId(), request.offeringId());
        // Implementation for registering a student for a class
        return ResponseEntity.ok(new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentStatus().name(),        //TODO: check if name() is appropriate here, or if we need id instead
                enrollment.getStudentId(),
                enrollment.getOfferingId()));

    }

    //post mapping for dropping classes
    @PostMapping("/drop")
    public ResponseEntity<EnrollmentResponse> drop(@RequestBody DropRequest request) {
        Enrollment enrollment = registrationService.dropClass(request.studentId(), request.offeringId());
        // Implementation for dropping a class
        return ResponseEntity.ok(new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentStatus().name(),        //TODO: check if name() is appropriate here, or if we need id instead
                enrollment.getStudentId(),
                enrollment.getOfferingId()));
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


    private CourseOfferingSearchResponse toCourseOfferingSearchResponse(CourseOfferingSearchResult result) {
        return CourseOfferingSearchResponse.builder()
                .offeringId(result.getOfferingId())
                .courseId(result.getCourseId())
                .courseCode(result.getCourseCode())
                .courseName(result.getCourseName())
                .semester(result.getSemester())
                .instructor(result.getInstructor())
                .startTime(result.getStartTime())
                .endTime(result.getEndTime())
                .daysTaught(result.getDaysTaught())
                .delivery(result.getDelivery())
                .location(result.getLocation())
                .seatCapacity(result.getSeatCapacity())
                .enrolled(result.getEnrolled())
                .section(result.getSection())
                .build();
    }
}
