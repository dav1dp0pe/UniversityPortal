package com.university.UniversityPortal.Repository.CourseRepository;

import com.university.UniversityPortal.Domain.Course.CoursePrerequisites;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CoursePrerequisiteJDBCRepository {
    private final JdbcTemplate jdbcTemplate;

    public CoursePrerequisiteJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CoursePrerequisites> findByCourseId(Long courseId) {
        String sql = """
                SELECT prerequisite_id,
                       course_id,
                       group_id,
                       prerequisite_type,
                       required_course_id,
                       min_grade_value,
                       required_standing
                FROM course_prerequisites
                WHERE course_id = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> CoursePrerequisites.builder()
                .prerequisiteId(rs.getLong("prerequisite_id"))
                .courseId(rs.getLong("course_id"))
                .groupId(rs.getInt("group_id"))
                .prerequisiteType(CoursePrerequisites.PrerequisteType.valueOf(rs.getString("prerequisite_type")))
                .requiredCourseId(rs.getLong("required_course_id"))
                .minGradeValue(rs.getObject("min_grade_value", Double.class))
                .requiredStanding(rs.getString("required_standing"))
                .build(), courseId);
    }

    public CoursePrerequisites save(CoursePrerequisites prerequisite) {
        if (prerequisite.getPrerequisiteId() == null) {
            return insert(prerequisite);
        }

        String sql = """
                UPDATE course_prerequisites
                SET course_id = ?,
                    group_id = ?,
                    prerequisite_type = ?,
                    required_course_id = ?,
                    min_grade_value = ?,
                    required_standing = ?
                WHERE prerequisite_id = ?
                """;

        jdbcTemplate.update(sql,
                prerequisite.getCourseId(),
                prerequisite.getGroupId(),
                prerequisite.getPrerequisiteType().name(),
                prerequisite.getRequiredCourseId(),
                prerequisite.getMinGradeValue(),
                prerequisite.getRequiredStanding(),
                prerequisite.getPrerequisiteId());

        return prerequisite;
    }

    private CoursePrerequisites insert(CoursePrerequisites prerequisite) {
        String sql = """
                INSERT INTO course_prerequisites
                (course_id, group_id, prerequisite_type, required_course_id, min_grade_value, required_standing)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, prerequisite.getCourseId());
            ps.setInt(2, prerequisite.getGroupId());
            ps.setString(3, prerequisite.getPrerequisiteType().name());
            ps.setObject(4, prerequisite.getRequiredCourseId());
            ps.setObject(5, prerequisite.getMinGradeValue());
            ps.setString(6, prerequisite.getRequiredStanding());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            prerequisite.setPrerequisiteId(key.longValue());
        }

        return prerequisite;
    }
}