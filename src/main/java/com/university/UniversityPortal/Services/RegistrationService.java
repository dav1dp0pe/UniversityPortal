package com.university.UniversityPortal.Services;

import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Repository.CourseOfferingRepository;
import com.university.UniversityPortal.Repository.EnrollmentRepository;
import com.university.UniversityPortal.Repository.StudentHoldRepository;
import com.university.UniversityPortal.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//TODO: add @transactional where needed
@Service
public class RegistrationService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final StudentHoldRepository studentHoldRepository;

    //constructor
    public RegistrationService(EnrollmentRepository enrollmentRepository,
                               StudentRepository studentRepository,
                               CourseOfferingRepository courseOfferingRepository, StudentHoldRepository studentHoldRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.studentHoldRepository = studentHoldRepository;
    }

    //service to register for classes
    //add checks for unique student IDs, valid course capacity, and prerequisites
    @Transactional
    public Enrollment registerForClass (Long studentId, Long offeringId) {

        //1. load student
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new RuntimeException("student not found: " + studentId));

        //TODO provide more detailed hold information
        //2. check for holds
        if(studentHoldRepository.existsByStudent_StudentIdAndActiveTrue(studentId)){
            throw new RuntimeException("Student has an active hold and cannot register.");
        }

        //3. lock offering row to prevent seat race condition
        CourseOffering offering = courseOfferingRepository.findByIdForUpdate(offeringId)
                .orElseThrow(() -> new RuntimeException("offering not found: " + offeringId));

        //4. check if already enrolled
        if(enrollmentRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(studentId, offeringId)) {
            throw new RuntimeException("student " + studentId + " is already enrolled in offering " + offeringId);
        }

        //5. check seat capacity
        long enrolledCount = enrollmentRepository.countActiveByOfferingId(offeringId);
        boolean hasSeats = enrolledCount < offering.getSeatCapacity();

        //TODO prerequisites looks sus
        //6. Prerequisites check
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

    //TODO: Map invalid dropped class to 400 or 409 error with an exception handler
    @Transactional
    public Enrollment dropClass(Long studentId, Long offeringId) {

        //find enrollment or waitlist record
        Enrollment enrollment = enrollmentRepository.findByStudent_StudentIdAndCourseOffering_OfferingIdAndEnrollmentStatusIn(
                studentId,
                offeringId,
                List.of(Enrollment.EnrollmentStatus.ENROLLED, Enrollment.EnrollmentStatus.WAITLISTED)
        ).orElseThrow(() -> new RuntimeException("enrollment/waitlist is not found for student " + studentId + " in offering " + offeringId));

        //store old status
        Enrollment.EnrollmentStatus oldStatus = enrollment.getEnrollmentStatus();

        //capture dropped position
        Integer droppedPosition = enrollment.getWaitlistPosition();

        //update enrollment to dropped
        enrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollment.setDroppedAt(LocalDateTime.now());
        enrollment.setLastUpdated(LocalDateTime.now());
        enrollment.setWaitlistPosition(null);
        enrollmentRepository.save(enrollment);

        //Only open up a spot if the student was enrolled, not waitlisted
        if(oldStatus == Enrollment.EnrollmentStatus.ENROLLED) {
            //promote next waitlisted student, if any
            enrollmentRepository.findFirstByCourseOffering_OfferingIdAndEnrollmentStatusOrderByWaitlistPositionAsc(offeringId, Enrollment.EnrollmentStatus.WAITLISTED)
                    .ifPresent(nextInLine -> {
                        nextInLine.setEnrollmentStatus(Enrollment.EnrollmentStatus.ENROLLED);
                        nextInLine.setWaitlistPosition(null);
                        nextInLine.setLastUpdated(LocalDateTime.now());
                        enrollmentRepository.save(nextInLine);

                        //decrement waitlist positions for those behind the promoted student
                        List<Enrollment> remaining = enrollmentRepository.findByCourseOffering_OfferingIdAndEnrollmentStatusAndWaitlistPositionGreaterThanOrderByWaitlistPositionAsc(
                                offeringId, Enrollment.EnrollmentStatus.WAITLISTED, 1);

                        for (Enrollment e : remaining) {
                            e.setWaitlistPosition(e.getWaitlistPosition() - 1);
                            e.setLastUpdated(LocalDateTime.now());
                        }
                        enrollmentRepository.saveAll(remaining);
                    });
            return enrollment;
        }


        //if the student was waitlisted, promote the next in line by adjusting their waitlist position
        else if(oldStatus == Enrollment.EnrollmentStatus.WAITLISTED && droppedPosition != null) {
            List<Enrollment> shifted = enrollmentRepository.findByCourseOffering_OfferingIdAndEnrollmentStatusAndWaitlistPositionGreaterThanOrderByWaitlistPositionAsc(
                    offeringId, Enrollment.EnrollmentStatus.WAITLISTED, droppedPosition);

            for (Enrollment e : shifted) {
                e.setWaitlistPosition(e.getWaitlistPosition() - 1);
                e.setLastUpdated(LocalDateTime.now());
            }
            enrollmentRepository.saveAll(shifted);
        }
        return enrollment;
    }

}