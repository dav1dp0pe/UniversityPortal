package com.university.UniversityPortal.feature.wishlist.repository;

import com.university.UniversityPortal.feature.wishlist.entity.Wishlist;
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
    wishlist.setOfferingId(rs.getLong("offering_id"));
    Timestamp ts = rs.getTimestamp("added_at");
    if (ts != null) {
        wishlist.setAddedAt(ts.toLocalDateTime());
    }
    return wishlist;
    }

}
