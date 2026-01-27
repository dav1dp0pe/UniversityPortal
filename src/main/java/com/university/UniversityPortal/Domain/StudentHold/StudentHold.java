package com.university.UniversityPortal.Domain.StudentHold;

import com.university.UniversityPortal.Domain.Student.Student;
//import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentHold {

   // @Id
   // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdId;

   // @ManyToOne(optional = false)
   // @JoinColumn(name = "student_id", nullable = false)
    private Long studentId;

    //@Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    public enum HoldType{
        FINANCIAL,
        ACADEMIC,
        DISCIPLINARY,
        LIBRARY,
        ADVISING,
        OTHER
    }

    private HoldType holdType;
    //@Column(nullable = false)
    private boolean active;

    private String reason;
    private LocalDateTime placedAt; //when hold was placed
    private LocalDateTime clearedAt;   //when hold was released
}
