package com.university.UniversityPortal.Domain.Student;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Program.AcademicProgram;
import com.university.UniversityPortal.Domain.Program.Minor;
import com.university.UniversityPortal.Domain.StudentHold.StudentHold;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

//@Entity
@Data
@NoArgsConstructor      //initializes an object using default values
@AllArgsConstructor     //automatically generates a constructor for Java class that includes parameters for every field
@Builder
//@Table(name = "students")

public class Student {

    //maps to students
    private Long studentId;

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
    private int creditsAttempted;

    private AcademicProgram major;
    private AcademicProgram minor;
    private LocalDateTime createdAt;

    /*
    //one student can have many enrollments
    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments;

    //one student can have many holds
    @OneToMany(mappedBy = "student")
    private List<StudentHold> holds;

    //TODO: student can only have one wishlist for 1 semester each. Need to enforce this in service layer.
    //one student can have many wishlists (over time)
    @OneToMany(mappedBy = "student")
    private List<Wishlist> wishlists;

    //relationship mapping to Major entity
    //TODO: What if student is a double major? What if student is a double minor?
    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;

    @ManyToOne
    @JoinColumn(name = "minor_id")
    private Minor minor;

*/
}
