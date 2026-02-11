package com.university.UniversityPortal.Repository.RowMappers;

import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
//import com.university.UniversityPortal.Domain.Wishlist.wishlist;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class WishlistRowMapper implements RowMapper<Wishlist> {
    @Override
    public Wishlist mapRow(ResultSet rs, int rowNum) throws SQLException {
    Wishlist wishlist = new Wishlist();
    wishlist.setWishlistId(rs.getLong("wishlist_id"));
    wishlist.setStudentId(rs.getLong("student_id"));
    wishlist.setSemester(rs.getString("semester"));
    return wishlist;
    }

}
