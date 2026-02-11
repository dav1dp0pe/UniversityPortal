package com.university.UniversityPortal.Repository.WishlistRepository;

import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Repository.RowMappers.WishlistRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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

    public Optional<Wishlist> findByStudentIdAndSemester(Long studentId, String semester) {
        String sql = """
                SELECT wishlist_id, student_id, semester
                FROM wishlist
                WHERE student_id = ? AND semester = ?
                """;

        List<Wishlist> results = jdbcTemplate.query(sql, rowMapper, studentId, semester);
        //TODO figure out the return type
        return results.stream().findFirst();
    }
    public Wishlist save(Wishlist item){
        String sql = """
                INSERT INTO wishlist (student_id, semester)
                VALUES(?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"wishlist_id"});

            ps.setLong(1, item.getStudentId());
            ps.setString(2, item.getSemester());
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


    /*
    TODO: implement these methods:
    List<Wishlist> findByStudent_StudentId(Long studentId);

    List<Wishlist> findByStudent_StudentIdAndCourseOffering_Term(Long studentId, String term);

    Optional<Wishlist> findByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);

    void deleteByStudent_StudentIdAndCourseOffering_OfferingId(Long studentId, Long offeringId);

    save, update, delete
     */
}
