package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

}
