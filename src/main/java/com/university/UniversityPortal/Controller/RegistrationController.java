package com.university.UniversityPortal.Controller;

import com.university.UniversityPortal.Controller.dto.*;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOfferingSearchResult;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {


    //TODO: Eventually send the Security Context (JWT token) to the service layer to validate student identity
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
    //student will input semester and course code
    @GetMapping("/offerings")
    public List<CourseOfferingSearchResponse> searchCourseOfferings(
            @RequestParam String semester,
            @RequestParam String courseCode){

        List<CourseOfferingSearchResult> results = registrationService.searchOfferingsBySemesterAndCourseCode(semester, courseCode);
        return results.stream().map(this::toCourseOfferingSearchResponse).toList();

    }

    //post mapping for registering for classes
    //this should create a new enrollment record and automatically request and receive the studentId, and offeringId
    //if successful, return the enrollment record created as response
    //if not successful, return an error message
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        try {
            Enrollment enrollment = registrationService.registerForClass(request.studentId(), request.offeringId());
            return ResponseEntity.status(201).body(new EnrollmentResponse(
                    enrollment.getId(),
                    enrollment.getEnrollmentStatus().name(),        //TODO: check if name() is appropriate here, or if we need id instead
                    enrollment.getStudentId(),
                    enrollment.getOfferingId(),
                    enrollment.getEnrolledAt()));
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
        // Implementation for registering a student for a class


    }

    //post mapping for dropping classes
    //this should update the enrollment record to reflect the dropped status for that studentId and offeringId
    @PostMapping("/drop")
    public ResponseEntity<EnrollmentResponse> drop(@RequestBody DropRequest request) {
        Enrollment enrollment = registrationService.dropClass(request.studentId(), request.offeringId());
        // Implementation for dropping a class
        return ResponseEntity.ok(new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrollmentStatus().name(),        //TODO: check if name() is appropriate here, or if we need id instead
                enrollment.getStudentId(),
                enrollment.getOfferingId(), enrollment.getEnrolledAt()));
    }

    //post mapping to add wishlist classes
    //this should create a new wishlist record for that studentId and offeringId
    @PostMapping("/wishlist/add")
    public ResponseEntity<Wishlist> addToWishlist(@RequestBody RegisterRequest request) {
        Wishlist wishlist = registrationService.addToWishlist(request.studentId(), request.offeringId());
        return ResponseEntity.status(201).body(wishlist);
    }

    //post mapping to remove wishlist classes
    //this should delete the wishlist record for that studentId and offeringId
    @DeleteMapping("/wishlist/remove")
    public ResponseEntity<Void> removeFromWishlist(@RequestBody RegisterRequest request) {
        registrationService.removeFromWishlist(request.studentId(), request.offeringId());
        return ResponseEntity.noContent().build();
    }

    //post mapping to register all classes from wishlist
    //this should attempt to register the student for all classes in their wishlist for the specified term
    //if successful, return a list of enrollment records created as response
    //if partially successful, return a list of enrollment records created and error messages for failed registrations
    //if not successful, return a list of error messages
    @PostMapping("/wishlist/register-all")
    public ResponseEntity<List<BatchRegistrationResult>> registerAll(@RequestParam Long studentId, @RequestParam String semester) {
        List<BatchRegistrationResult> results = registrationService.registerAllFromWishlist(studentId, semester);
        return ResponseEntity.ok(results);
    }

    //global exception handler for the controller
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // Log the exception (not shown here for brevity)
        return ResponseEntity.badRequest().body(e.getMessage());
    }


    //utility method to convert CourseOfferingSearchResult to CourseOfferingSearchResponse
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

    //TODO: if registrationService throws exceptions, add exception handlers here
    // look into @ControllerAdvice for global exception handling
}
