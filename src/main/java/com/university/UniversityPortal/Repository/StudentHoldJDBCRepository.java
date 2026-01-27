package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.StudentHold.StudentHold;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
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

    public StudentHold save(StudentHold studentHold){
        if (studentHold.getHoldId() == null) {
            return insert(studentHold);
        } else {
            update(studentHold);
            return studentHold;
        }
    }

    private StudentHold insert(StudentHold studentHold){
        String sql = """
                INSERT INTO student_hold
                    (student_id,
                     hold_type,
                     reason,
                     active,
                     placed_at,
                     cleared_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                studentHold.getStudentId(),
                studentHold.getHoldType() != null ? studentHold.getHoldType().name() : null,
                studentHold.getReason(),
                studentHold.isActive(),
                studentHold.getPlacedAt() != null ? Timestamp.valueOf(studentHold.getPlacedAt()) : null,
                studentHold.getClearedAt() != null ? Timestamp.valueOf(studentHold.getClearedAt()) : null
        );

        return studentHold;
    }

    private void update(StudentHold studentHold){
        String sql = """
                UPDATE student_hold
                SET hold_type = ?,
                    reason = ?,
                    active = ?,
                    placed_at = ?,
                    cleared_at = ?
                WHERE hold_id = ?
                """;

        jdbcTemplate.update(sql,
                studentHold.getHoldType(),
                studentHold.getReason(),
                studentHold.isActive(),
                studentHold.getPlacedAt(),
                studentHold.getClearedAt(),
                studentHold.getStudentId()
        );
    }

}
