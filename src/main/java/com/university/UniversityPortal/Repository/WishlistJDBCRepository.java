package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Domain.Wishlist.WishlistItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class WishlistJDBCRepository {
    private final JdbcTemplate jdbcTemplate;
    private final WishlistRowMapper rowMapper = new WishlistRowMapper();

    public WishlistJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*
    public List<WishlistItem> findByStudentId(Long studentId) {
        String sql = """
                SELECT *
                FROM wishlist
                WHERE student_id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, studentId);
    }
    */

    public Long count(){
        String sql = "SELECT COUNT(*) FROM wishlist";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    public boolean existsByStudentIdAndOfferingId(Long studentId, Long offeringId) {
        String sql = """
                SELECT COUNT(*)
                FROM wishlist
                WHERE student_id = ? AND offering_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, offeringId);

        //TODO figure out the return type
        return count != null && count > 0;
    }

    public Wishlist save(Wishlist item){
        String sql = """
                INSERT INTO wishlist (student_id, offering_id, added_at)
                VALUES(?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"wishlist_id"});

            ps.setLong(1, item.getStudentId());
            ps.setLong(2, item.getOfferingId());
            ps.setTimestamp(3, Timestamp.valueOf(item.getAddedAt()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if(key != null){
            item.setWishlistId(key.longValue());
        }
        return item;
    }

    public int deleteByStudentIdAndOfferingId(Long studentId, Long offeringId){
        String sql = """
                DELETE FROM wishlist
                WHERE student_id = ? AND offering_id = ?
                """;

        return jdbcTemplate.update(sql, studentId, offeringId); //return number of rows deleted
    }

    public List<Wishlist> findByStudentIdAndTerm(Long studentId, String semester){
        String sql = """
                SELECT w.wishlist_id, w.student_id, w.offering_id, w.added_at
                FROM wishlist w
                JOIN course_offering co
                ON w.offering_id = co.offering_id
                WHERE w.student_id = ?
                AND co.semester = ?
                """;

        return jdbcTemplate.query(sql, rowMapper, studentId, semester);
    }
    /*
    TODO: implement these methods:
    List<Wishlist> findByStudent_StudentId(Long studentId);

    List<Wishlist> findByStudent_StudentIdAndCourseOffering_Term(Long studentId, String term);

    Optional<Wishlist> findByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);

    void deleteByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);

    save, update, delete
     */
}
