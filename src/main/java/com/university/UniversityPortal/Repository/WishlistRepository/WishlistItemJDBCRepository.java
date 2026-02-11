package com.university.UniversityPortal.Repository.WishlistRepository;

import com.university.UniversityPortal.Domain.Wishlist.WishlistItem;
import com.university.UniversityPortal.Repository.RowMappers.WishlistItemRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class WishlistItemJDBCRepository {
    private final JdbcTemplate jdbcTemplate;
    private final WishlistItemRowMapper rowMapper = new WishlistItemRowMapper();

    public WishlistItemJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByStudentIdAndOfferingId(Long studentId, Long offeringId) {
        String sql = """
                SELECT COUNT(*)
                FROM wishlist_item wi
                JOIN wishlist w ON wi.wishlist_id = w.wishlist_id
                WHERE w.student_id = ? AND wi.offering_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, offeringId);
        return count != null && count > 0;
    }

    public WishlistItem save(WishlistItem item) {
        String sql = """
                INSERT INTO wishlist_item (wishlist_id, offering_id, added_at)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, item.getWishlistId());
            ps.setLong(2, item.getOfferingId());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(item.getAddedAt()));
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("wishlist_item_id")) {
            item.setWishlistItemId(((Number) keys.get("wishlist_item_id")).longValue());
        }
        return item;
    }

    public int deleteByStudentIdAndOfferingId(Long studentId, Long offeringId) {
        String sql = """
                DELETE FROM wishlist_item
                WHERE offering_id = ?
                AND wishlist_id IN (
                    SELECT wishlist_id
                    FROM wishlist
                    WHERE student_id = ?
                )
                """;
        return jdbcTemplate.update(sql, offeringId, studentId);
    }

    public List<WishlistItem> findByStudentIdAndSemester(Long studentId, String semester) {
        String sql = """
                SELECT wi.wishlist_item_id, wi.wishlist_id, wi.offering_id, wi.added_at
                FROM wishlist_item wi
                JOIN wishlist w ON wi.wishlist_id = w.wishlist_id
                WHERE w.student_id = ? AND w.semester = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, studentId, semester);
    }

    public int countByStudentIdAndSemester(Long studentId, String semester) {
        String sql = """
                SELECT COUNT(*)
                FROM wishlist_item wi
                JOIN wishlist w ON wi.wishlist_id = w.wishlist_id
                WHERE w.student_id = ? AND w.semester = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, semester);
        return count != null ? count : 0;
    }
}
