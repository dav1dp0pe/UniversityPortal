package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByFirstNameAndMiddleName(String firstName, String middleName);
}
