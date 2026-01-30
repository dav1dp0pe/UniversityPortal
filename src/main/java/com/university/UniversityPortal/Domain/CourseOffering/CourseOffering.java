package com.university.UniversityPortal.Domain.CourseOffering;

//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Table(
//        name = "course_offering",
 //       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "term", "section"} )
//)
public class CourseOffering {

    //TODO:
    // is offeringId and section redundant???
    private Long offeringId;

    //@ManyToOne(optional = false)
    //@JoinColumn(name = "course_id", nullable = false)
    private Long courseId;

    //@Column(nullable = false)
    private String semester;        //Spring 2026, Fall 2025, Summer 2026

    private String instructor;

    private String startTime;
    private String endTime;
    private String daysTaught;
    private String dateRange;
    private String delivery;
    private String location;
    private int seatCapacity;
    private int enrolled;
    private short section;      // e.g., 1, 2, 3 for different sections of the same course in a term



    // @Column(nullable = false)

   // @Column(nullable = false)
   // @OneToMany(mappedBy = "courseOffering")
    //TODO, change enrollments list???
    //private List<Enrollment> enrollments;
}
