package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByStudent_StudentId(Long studentId);

    List<Wishlist> findByStudent_StudentIdAndCourseOffering_Term(Long studentId, String term);

    Optional<Wishlist> findByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);

    void deleteByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);
}
