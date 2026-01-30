package com.university.UniversityPortal.Domain.Program;

//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//declare Major class as an entity so that it can be mapped to a database table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Minor {
   // @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String school;  //e.g., School of Engineering, School of Arts
}
