package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Program.Minor;
import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinorRepository extends JpaRepository<Minor, Integer> {

}
