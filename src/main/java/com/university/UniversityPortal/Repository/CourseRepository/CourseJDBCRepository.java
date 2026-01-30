package com.university.UniversityPortal.Repository.CourseRepository;

import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Repository.RowMappers.CourseRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CourseJDBCRepository {

    // Constructor
    private final JdbcTemplate jdbcTemplate;

    public CourseJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //TODO
    // potentially make Optional???

    //TODO if we search for a course by id, the corresponding offerings should appear as well
    public Course findCourseById(Long courseId) {
        String sql = "SELECT course_id, course_name, course_code, credit_hours " +
                     "FROM courses WHERE course_id = ?";
        return jdbcTemplate.queryForObject(sql, new CourseRowMapper(), courseId);
    }

    public List<Long> findPrerequisiteCourseIds(Long courseId){
        String sql = """
                SELECT prerequisite_id
                FROM course_prerequisites
                WHERE course_id = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("prerequisite_id"), courseId);
    }

    public Course save(Course course) {
        if (course.getCourseId() == null) {
            return insert(course);
        } else {
            update(course);
            return course;
        }
    }

    public Course insert(Course course) {
        String sql = """
                INSERT INTO courses
                (course_name, course_code,
                credit_hours, semesters_taught,
                description, repeatable, placement_exam_available,
                general_education_course)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, course.getCourseName());
            ps.setString(2, course.getCourseCode());
            ps.setInt(3, course.getCreditHours());
            ps.setString(4, course.getSemestersTaught());
            ps.setString(5, course.getDescription());
            ps.setBoolean(6, course.isRepeatable());
            ps.setBoolean(7, course.isPlacementExamAvailable());
            ps.setString(8, course.getGeneralEducationCourse());
            return ps;
            }, keyHolder);

        Number key = keyHolder.getKey();
        if(key != null) {
            course.setCourseId(key.longValue());
        }

        return course;
    }

    private void update(Course course) {
        String sql = """
                UPDATE courses
                SET course_name = ?,
                course_code = ?,
                credit_hours = ?,
                semesters_taught = ?,
                description = ?,
                repeatable = ?,
                placement_exam_available = ?,
                general_education_course = ?
                WHERE course_id = ?
                """;

        jdbcTemplate.update(sql,
                course.getCourseName(),
                course.getCourseCode(),
                course.getCreditHours(),
                course.getSemestersTaught(),
                course.getDescription(),
                course.isRepeatable(),
                course.isPlacementExamAvailable(),
                course.getGeneralEducationCourse(),
                course.getCourseId()
                );
    }


    //TODO: add more JDBC methods as needed
    //TODO: Decide which relationships will be loaded manually via SQL joins or separate queries
    //TODO: document how each relationship is loaded (“enrollments loaded via EnrollmentRepository.findByStudentId”)
    // add save, update, delete methods
}

