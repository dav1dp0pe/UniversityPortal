package com.university.UniversityPortal.feature.enrollment.entity;

//join between student and course

import com.university.UniversityPortal.feature.student.entity.Student;
import com.university.UniversityPortal.feature.courseoffering.entity.CourseOffering;

//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//unique constraint to prevent duplicate enrollments for the same student in the same course and term
//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Table(name = "enrollment",
//        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "offering_id"}))
public class Enrollment {

 //   @Id
 //   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //many enrollments can belong to one student
 //   @ManyToOne(optional = false)
 //   @JoinColumn(name = "student_id")
    private Long studentId;

    //many enrollments can belong to one course
 //   @ManyToOne
 //   @JoinColumn(name = "offering_id", nullable = false)
    private Long offeringId;

    //private String term;  //Fall 2025, Spring 2026, Summer

    //private String status; //Enrolled, Waitlisted, Dropped, Completed

    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
    private String grade; //A, B, C, D, F, Incomplete
    private LocalDateTime lastUpdated;

    //waitlist position if applicable
    private Integer waitlistPosition;
    private int creditsAttempted;

    public enum EnrollmentStatus {
        ENROLLED,
        WAITLISTED,
        DROPPED,
        COMPLETED
    }

 //   @Enumerated(EnumType.STRING)
    private EnrollmentStatus enrollmentStatus;
}
