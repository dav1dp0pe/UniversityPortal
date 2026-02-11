package com.university.UniversityPortal.Repository.StudentRepository;

import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Repository.RowMappers.StudentRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentJDBCRepository {

    //constructor
    private final JdbcTemplate jdbcTemplate;
    private final StudentRowMapper studentRowMapper = new StudentRowMapper();

    public StudentJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Student> findStudentById(Long studentId) {
        String sql = """
                SELECT * 
                FROM students 
                WHERE student_id = ?
        """;
        List<Student> result = jdbcTemplate.query(sql, studentRowMapper, studentId);
        return result.stream().findFirst();
    }

    public List<Student> findAll() {
        String sql = """
                SELECT *
                FROM students
                """;
        return jdbcTemplate.query(sql, studentRowMapper);
    }

    public Optional<Student> findByEmail(String email) {
        String sql = """
                SELECT *
                FROM students
                WHERE email = ?
                """;
        List<Student> result = jdbcTemplate.query(sql, studentRowMapper, email);
        return result.stream().findFirst();
    }

    public Student save(Student student) {
        if (student.getStudentId() == null) {
            return insert(student);
        } else {
            update(student);
            return student;
        }
    }

    private Student insert(Student student) {
        String sql = """
                INSERT INTO students
                    (first_name, middle_name, last_name,
                    date_of_birth, status, email, phone_number,
                    address, gpa, credits_completed, credits_attempted,
                    major_id, minor_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"student_id"});
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getMiddleName());
            ps.setString(3, student.getLastName());
            if (student.getDateOfBirth() != null) {
                ps.setDate(4, Date.valueOf(student.getDateOfBirth()));
            } else {
                ps.setDate(4, null);
            }
            ps.setString(5, student.getStatus());
            ps.setString(6, student.getEmail());
            ps.setString(7, student.getPhoneNumber());
            ps.setString(8, student.getAddress());
            ps.setFloat(9, student.getGpa());
            ps.setInt(10, student.getCreditsCompleted());
            ps.setInt(11, student.getCreditsAttempted());

            //todo: change getMajorId to getProgramId???
            if (student.getMajor() != null && student.getMajor().getMajorId() != null) {
                ps.setLong(12, student.getMajor().getMajorId());
            } else {
                ps.setNull(12, java.sql.Types.BIGINT);
            }

            if (student.getMinor() != null && student.getMinor().getMajorId() != null) {
                ps.setLong(13, student.getMinor().getMajorId());
            } else {
                ps.setNull(13, java.sql.Types.BIGINT);
            }

            return ps;
        }, keyHolder);

        Number key = (Number) keyHolder.getKeys().get("student_id");

        if (key != null) {
            student.setStudentId(key.longValue());
        }

        return student;
    }

    private void update(Student student) {
        String sql = """
                UPDATE students
                   SET first_name = ?,
                   middle_name = ?,
                   last_name = ?,
                   date_of_birth = ?,
                   status = ?,
                   email = ?,
                   phone_number = ?,
                   address = ?,
                   gpa = ?,
                   credits_completed = ?,
                   credits_attempted = ?,
                   major_id = ?,
                   minor_id = ?
                WHERE student_id = ?
                """;

        jdbcTemplate.update(sql,
                student.getFirstName(),
                student.getMiddleName(),
                student.getLastName(),
                student.getDateOfBirth() != null ? Date.valueOf(student.getDateOfBirth()) : null,
                student.getStatus(),
                student.getEmail(),
                student.getPhoneNumber(),
                student.getAddress(),
                student.getGpa(),
                student.getCreditsCompleted(),
                student.getCreditsAttempted(),
                (student.getMajor() != null) ? student.getMajor().getMajorId() : null,
                (student.getMinor() != null) ? student.getMinor().getMajorId() : null,
                student.getStudentId()
        );
    }

    public void delete(Long studentId) {
        String sql = """
                DELETE FROM students
                WHERE student_id = ?
        """;
        jdbcTemplate.update(sql, studentId);
    }
}
