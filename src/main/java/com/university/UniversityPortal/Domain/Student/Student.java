package com.university.UniversityPortal.Domain.Student;


import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Program.Major;
import com.university.UniversityPortal.Domain.Program.Minor;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "students")

public class Student {

    //use Identity to force the database's auto increment feature
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long studentId;

    private String firstName;
    private String middleName;
    private String lastName;

    private LocalDate dateOfBirth;
    private String status;      //Active, Inactive, Graduated, etc.

    /*
    //JPA cannot persist lists directly, need a join table for many-to-many relationship
    //For registration purposes, the model is Student <--> Enrollment <--> CourseOffering
    @ManyToMany
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )

     */

    private String email;
    private String phoneNumber;
    private String address;

    private float gpa;
    private int creditsCompleted;

    //one student can have many enrollments
    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments;

    //relationship mapping to Major entity
    //TODO: What if student is a double major? What if student is a double minor?
    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;

    @ManyToOne
    @JoinColumn(name = "minor_id")
    private Minor minor;

}
