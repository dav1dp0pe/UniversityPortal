package com.university.UniversityPortal.Domain.CourseOffering;

import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(
        name = "course_offering",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "term", "section"} )
)
public class CourseOffering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offeringId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false)
    private short section;      // e.g., 1, 2, 3 for different sections of the same course in a term

    @Column(nullable = false)
    private int seatCapacity;

    private String instructorName;
    private String days;
    private String startTime;
    private String endTime;
    private String room;

    @OneToMany(mappedBy = "courseOffering")
    private List<Enrollment> enrollments;
}
