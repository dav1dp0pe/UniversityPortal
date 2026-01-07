package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);

    // Custom query to count enrollments by offering ID with specific statuses
    @Query("""
            SELECT count(e)
            FROM Enrollment e
            WHERE e.courseOffering.offeringId = :offeringId
            AND e.enrollmentStatus in ('ENROLLED', 'WAITLISTED')
            """)
    long countActiveByOfferingId(@Param("offeringId") long offeringId);


    // Custom query to count completed courses for a student given a list of course IDs
    @Query("""
            SELECT count(e)
            FROM Enrollment e
            WHERE e.student.studentId = :studentId
            AND e.enrollmentStatus = 'COMPLETED'
            AND e.courseOffering.course.courseId IN :courseIds
            """)
    long countCompletedCourses(@Param("studentId") Long studentId, @Param("courseIds") List<Long> courseIds);


    Optional<Enrollment> findByStudent_StudentIdAndCourseOffering_OfferingIdAndEnrollmentStatusIn(Long studentId, Long offeringId, List<Enrollment.EnrollmentStatus> statuses);

    Optional<Enrollment> findFirstByCourseOffering_OfferingIdAndEnrollmentStatusOrderByWaitlistPositionAsc(Long offeringId, Enrollment.EnrollmentStatus status);

    List<Enrollment> findByCourseOffering_OfferingIdAndEnrollmentStatusAndWaitlistPositionGreaterThanOrderByWaitlistPositionAsc(Long offeringId, Enrollment.EnrollmentStatus status, Integer waitlistPosition);

    Long student(Student student);

    //TODO: Add method to count only waitlisted students for a course offering

    //TODO: keep enrolled seat check based on enrolled count only

    //TODO: add method to decrement waitlist positions after a drop
}
