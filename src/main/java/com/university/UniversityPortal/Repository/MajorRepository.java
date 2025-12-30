package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Program.Major;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
}
