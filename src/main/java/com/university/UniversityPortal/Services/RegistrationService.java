package com.university.UniversityPortal.Services;

import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Repository.CourseOfferingRepository;
import com.university.UniversityPortal.Repository.EnrollmentRepository;
import com.university.UniversityPortal.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//TODO: add @transactional where needed
@Service
public class RegistrationService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseOfferingRepository courseOfferingRepository;

    //constructor
    public RegistrationService(EnrollmentRepository enrollmentRepository,
                               StudentRepository studentRepository,
                               CourseOfferingRepository courseOfferingRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseOfferingRepository = courseOfferingRepository;
    }

    //service to register for classes
    //add checks for unique student IDs, valid course capacity, and prerequisites
    @Transactional
    public Enrollment registerForClass (Long studentId, Long offeringId) {

        //1. load student
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("student not found: " + studentId));

        //2. lock offering row to prevent seat race condition
        CourseOffering offering = courseOfferingRepository.findByIdForUpdate(offeringId)
                .orElseThrow(() -> new RuntimeException("offering not found: " + offeringId));

        //3. check if already enrolled
        if(enrollmentRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(studentId, offeringId)) {
            throw new RuntimeException("student " + studentId + " is already enrolled in offering " + offeringId);
        }

        //4. check seat capacity
        long enrolledCount = enrollmentRepository.countActiveByOfferingId(offeringId);
        boolean hasSeats = enrolledCount < offering.getSeatCapacity();

        //TODO prerequisites looks sus
        //5. Prerequisites check
        Course course = offering.getCourse();
        if(course.getPrerequisites() != null && !course.getPrerequisites().isEmpty()) {
            for(Course prereq : course.getPrerequisites()) {
                long completed = enrollmentRepository.countCompletedCourses(studentId, List.of(prereq.getCourseId()));  //TODO: do we need List.of()?
                if (completed == 0) {
                    throw new RuntimeException("student " + studentId + " has not completed prerequisite " + prereq.getCourseCode() + " for offering " + offeringId);
                }
            }
        }

        //6. create enrollment record
        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourseOffering(offering);
        e.setEnrolledAt(java.time.LocalDateTime.now());

        if (hasSeats) {
            e.setEnrollmentStatus(Enrollment.EnrollmentStatus.ENROLLED);
        } else {
            e.setEnrollmentStatus(Enrollment.EnrollmentStatus.WAITLISTED);
            //set waitlist position
            long waitlistPosition = enrolledCount - offering.getSeatCapacity() + 1;
            e.setWaitlistPosition((int) waitlistPosition);
        }

        return enrollmentRepository.save(e);
    }


}