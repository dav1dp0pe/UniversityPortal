package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
}
