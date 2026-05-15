package com.university.UniversityPortal.feature.course.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePrerequisites {
    private Long prerequisiteId;
    private Long courseId;

    private Long requiredCourseId;  // the course that is required as a prerequisite
    private PrerequisteType prerequisiteType; // ENUM: COURSE, GRADE, STANDING

    public enum PrerequisteType{
        COURSE,
        GRADE,
        STANDING,
        GPA,
        CREDIT_HOURS
    }

    private Double minGradeValue;   // e.g., 2.0 for C, 3.0 for B

    private String requiredStanding; //e.g., "Senior", "Junior"

    private int groupId;        //prerequisites in the same group are "OR"ed together, different groups are "AND"ed together
}
