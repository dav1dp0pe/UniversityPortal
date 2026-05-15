package com.university.UniversityPortal.feature.course.entity;

//import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Course {

    //maps to courses.course_id (auto-increment in DB)
    private Long courseId;
    private String courseName;
    private String courseCode;
    private int creditHours;
    private String semestersTaught;
    private String program;     //undergraduate, graduate, certificate
    private String department;
    private String description;
    private boolean repeatable;
    private boolean placementExamAvailable;
    private String generalEducationCourse;

    //TODO
    // Not auto-mapped; you'd fill this by calling another repository method
    // after loading the Course itself.
    // private List<CoursePrerequisites> prerequisites = new ArrayList<>();
}

//TODO: Decide how a course becomes "Completed" - Is it through Enrollment status or CourseOffering?
//TODO: Add simple controller endpoints for CRUD operations on Course entity (ex: POST /students/{studentId}/offerings/{offeringId}/register) to add a course for a student)
//TODO: Add test cases to register when seats exist, waitlists when full, blocks when prerequisites not met
