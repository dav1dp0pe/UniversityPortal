package com.university.UniversityPortal.Domain.StudentHold;

import com.university.UniversityPortal.Domain.Student.Student;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_hold")
@Data
public class StudentHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldType holdType;

    @Column(nullable = false)
    private boolean active;

    private String reason;
    private LocalDateTime placedAt; //when hold was placed
    private LocalDateTime releasedAt;   //when hold was released
}
