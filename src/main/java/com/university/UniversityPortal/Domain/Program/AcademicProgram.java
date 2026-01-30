package com.university.UniversityPortal.Domain.Program;

//import jakarta.persistence.*;
import com.university.UniversityPortal.Domain.Course.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

//declare Major class as an entity so that it can be mapped to a database table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicProgram {
    //@Id
   //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long majorId;

    private String name;
    private String programType; //Major, Minor, Masters
    private String degreeType; //e.g., Bachelor of Science, Bachelor of Arts
    private String school;  //e.g., School of Engineering, School of Arts

    //TODO: map relationship to the required courses
    private List<Course> requiredCourses = new ArrayList<>();
}
