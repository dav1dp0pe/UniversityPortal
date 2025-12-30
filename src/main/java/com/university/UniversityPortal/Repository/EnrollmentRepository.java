package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndOfferingId(Long studentId, Long offeringId);

    // Custom query to count enrollments by offering ID with specific statuses
    @Query("""
            SELECT count(e)
            FROM Enrollment e
            WHERE e.courseOffering.offeringId = :offeringId
            AND e.status in ('ENROLLED', 'WAITLISTED')
            """)
    long countByOfferingId(@Param("offeringId") long offeringId);


    // Custom query to count completed courses for a student given a list of course IDs
    @Query("""
            SELECT count(e)
            FROM Enrollment e
            WHERE e.student.studentId = :studentId
            AND e.status = 'COMPLETED'
            AND e.courseOffering.course.courseId in :courseId
            """)
    long countCompletedCourses(@Param("studentId") long studentId, @Param("courseId") List<Long> courseIds);
}
