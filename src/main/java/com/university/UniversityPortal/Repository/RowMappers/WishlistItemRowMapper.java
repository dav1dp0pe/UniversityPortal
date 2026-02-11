package com.university.UniversityPortal.Repository.RowMappers;

import com.university.UniversityPortal.Domain.Wishlist.WishlistItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class WishlistItemRowMapper implements RowMapper<WishlistItem> {
    @Override
    public WishlistItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        WishlistItem item = new WishlistItem();
        item.setWishlistItemId(rs.getLong("wishlist_item_id"));
        item.setWishlistId(rs.getLong("wishlist_id"));
        item.setOfferingId(rs.getLong("offering_id"));
        Timestamp ts = rs.getTimestamp("added_at");
        if (ts != null) {
            item.setAddedAt(ts.toLocalDateTime());
        }
        return item;
    }
}