package com.university.UniversityPortal.Domain.Program;

import jakarta.persistence.*;
import lombok.Data;

//declare Major class as an entity so that it can be mapped to a database table
@Entity
@Data
@Table(name = "minor")
public class Minor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String school;  //e.g., School of Engineering, School of Arts
}
