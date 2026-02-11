package com.university.UniversityPortal.Domain.Wishlist;


import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Student.Student;
//import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

//@Entity
@Data

/*@Table(
        name = "wishlist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "offering_id"})
        }
   )
 */
//TODO potentially change name to "WishlistItem" to avoid confusion with the entire wishlist
public class Wishlist {

    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    //@ManyToOne(optional = false)
    //@JoinColumn(name = "student_id", nullable = false)
    private Long studentId;

    //TODO add offeringId to table
    private String semester;
    private List<WishlistItem> items;
}
