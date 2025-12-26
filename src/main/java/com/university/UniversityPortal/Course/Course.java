package com.university.UniversityPortal.Course;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Data
@Table(name = "course")

public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long courseId;
    private String courseName;
    private String courseCode;
    private int seatCapacity;
    // Add a field for prerequisites
    private String prerequisites;
}
