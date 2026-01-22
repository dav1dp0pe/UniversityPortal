package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StudentHoldJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentHoldJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //TODO: implementation
    public boolean existsActiveByStudentId(Long studentId){
        String sql = """
                SELECT COUNT(*)
                FROM student_hold
                WHERE student_id = ?
                AND active = true
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId);
        return count != null && count > 0;      //returns true if any record exists matching student_id, active = true
    }

}
