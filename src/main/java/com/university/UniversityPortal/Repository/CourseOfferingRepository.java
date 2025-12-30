package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

    // Pessimistic lock to prevent concurrent modifications
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CourseOffering o WHERE o.offeringId = :id")   //select by id for update
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);
}
