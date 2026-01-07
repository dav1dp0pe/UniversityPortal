package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Domain.StudentHold.HoldType;
import com.university.UniversityPortal.Domain.StudentHold.StudentHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentHoldRepository extends JpaRepository<StudentHold, Long> {

    boolean existsByStudent_StudentIdAndActiveTrue(Long studentId);

    boolean existsByStudent_StudentIdAndHoldTypeAndActiveTrue(Long studentId, HoldType holdType);

    List<StudentHold> findByStudent_StudentIdAndActiveTrue(Long studentId);
}
