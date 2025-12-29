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
    private int seatCapacity;
    private int section;
    private short sectionNumber;
    private String term;  //Fall, Spring, Summer
    private int creditHours;

    // Add a field for prerequisites
    @ManyToMany
    @JoinTable(
            name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )

    //one course can have many enrollments
    @OneToMany(mappedBy = "course")
    private List<Enrollment> enrollments;


    private List<Course> prerequisites;
}
