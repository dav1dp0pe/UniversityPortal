package com.university.UniversityPortal.Domain.Course;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Table(name = "courses")

public class Course {

    //use Identity to force the database's auto increment feature
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String courseName;
    private String courseCode;
    //private int seatCapacity;
    //private int section;
    //private short sectionNumber;
    //private String term;  //Fall, Spring, Summer
    private int creditHours;



    //one course can have many enrollments
   /* @OneToMany(mappedBy = "course")
    private List<Enrollment> enrollments; */

    // Add a field for prerequisites
    @ManyToMany
    @JoinTable(
            name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private List<Course> prerequisites;
}

//TODO: Decide how a course becomes "Completed" - Is it through Enrollment status or CourseOffering?
//TODO: Add simple controller endpoints for CRUD operations on Course entity (ex: POST /students/{studentId}/offerings/{offeringId}/register) to add a course for a student)
//TODO: Add test cases to register when seats exist, waitlists when full, blocks when prerequisites not met