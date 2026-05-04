package com.university.UniversityPortal.Repository.EnrollmentRepository;

import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment.EnrollmentStatus;
import com.university.UniversityPortal.Repository.RowMappers.EnrollmentRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EnrollmentJDBCRepository {


    private final JdbcTemplate jdbcTemplate;

    private final EnrollmentRowMapper rowMapper = new EnrollmentRowMapper();

    private static final String BASE_SELECT = """
            SELECT enrollment_id, student_id, offering_id,
                           enrolled_at, dropped_at, grade,
                           last_updated, waitlist_position,
                           credits_attempted, enrollment_status
                    FROM enrollments
            """;

    public EnrollmentJDBCRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Enrollment> findById(Long id) {
        String sql = BASE_SELECT + "WHERE enrollment_id = ?";
        List<Enrollment> result = jdbcTemplate.query(sql, rowMapper, id);
        return result.stream().findFirst();
    }

    public int count(){
        String sql = "SELECT COUNT(*) FROM enrollments";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public Enrollment save(Enrollment enrollment) {
        if (enrollment.getId() == null) {
            return insert(enrollment);
        } else {
            update(enrollment);
            return enrollment;
        }
    }

    public EnrollmentStatus getEnrollmentStatusByStudentId(Long studentId, Long offeringId){
        String sql = """
                SELECT enrollment_status
                FROM enrollments
                WHERE student_id = ?
                AND offering_id = ?
                """;

        List<String> result = jdbcTemplate.queryForList(sql, String.class, studentId, offeringId);
        return result.stream().findFirst().map(EnrollmentStatus::valueOf).orElse(null);
    }

    private Enrollment insert(Enrollment enrollment) {
        String sql = """
            INSERT INTO enrollments
              (student_id, offering_id, enrolled_at, dropped_at, grade,
               last_updated, waitlist_position, credits_attempted, enrollment_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"enrollment_id"});
            ps.setLong(1, enrollment.getStudentId());
            ps.setLong(2, enrollment.getOfferingId());
            if (enrollment.getEnrolledAt() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(enrollment.getEnrolledAt()));
            } else {
                ps.setNull(3, java.sql.Types.TIMESTAMP);
            }
            if (enrollment.getDroppedAt() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(enrollment.getDroppedAt()));
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }
            ps.setString(5, enrollment.getGrade());
            if (enrollment.getLastUpdated() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(enrollment.getLastUpdated()));
            } else {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            }
            if (enrollment.getWaitlistPosition() != null) {
                ps.setInt(7, enrollment.getWaitlistPosition());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setInt(8, enrollment.getCreditsAttempted());
            ps.setString(9, enrollment.getEnrollmentStatus() != null ? enrollment.getEnrollmentStatus().name() : null);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            enrollment.setId(keyHolder.getKey().longValue());
        }
        return enrollment;
    }

    public void update(Enrollment enrollment) {
        String sql = """
            UPDATE enrollments
            SET student_id = ?,
                offering_id = ?,
                enrolled_at = ?,
                dropped_at = ?,
                grade = ?,
                last_updated = ?,
                waitlist_position = ?,
                credits_attempted = ?,
                enrollment_status = ?
            WHERE enrollment_id = ?
            """;

        Timestamp enrolledAt = enrollment.getEnrolledAt() != null ? Timestamp.valueOf(enrollment.getEnrolledAt()) : null;
        Timestamp droppedAt = enrollment.getDroppedAt() != null ? Timestamp.valueOf(enrollment.getDroppedAt()) : null;
        Timestamp lastUpdated = enrollment.getLastUpdated() != null ? Timestamp.valueOf(enrollment.getLastUpdated()) : null;

//TODO timestamp.valueOf is sus
        jdbcTemplate.update(sql,
                enrollment.getStudentId(),
                enrollment.getOfferingId(),
                enrolledAt,
                droppedAt,
                enrollment.getGrade(),
                lastUpdated,
                enrollment.getWaitlistPosition(),
                enrollment.getCreditsAttempted(),
                enrollment.getEnrollmentStatus() != null ? enrollment.getEnrollmentStatus().name() : null,
                enrollment.getId()
        );
    }

    // JPA: existsByStudent_StudentIdAndCourseOffering_OfferingId(...)
    public boolean existsByStudentIdAndOfferingId(Long studentId, Long offeringId) {
        String sql = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?
              AND offering_id = ?
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, studentId, offeringId);
        return count != null && count > 0;
    }

    // JPA: countActiveByOfferingId(...)
    // active = ENROLLED or WAITLISTED
    public long countActiveByOfferingId(long offeringId) {
        String sql = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE offering_id = ?
              AND enrollment_status IN ('ENROLLED', 'WAITLISTED')
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, offeringId);
        return count != null ? count : 0L;
    }

    // JPA: countCompletedCourses(studentId, courseIds)
    // requires join enrollment -> course_offering -> course
    public long countCompletedCourses(Long studentId, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return 0L;
        }

        String inClause = makePlaceholders(courseIds.size());

        String sql = """
            SELECT COUNT(*)
            FROM enrollments e
            JOIN course_offering co ON e.offering_id = co.offering_id
            WHERE e.student_id = ?
              AND e.enrollment_status = 'COMPLETED'
              AND co.course_id IN (%s)
            """.formatted(inClause);

        Object[] params = new Object[1 + courseIds.size()];
        params[0] = studentId;
        for (int i = 0; i < courseIds.size(); i++) {
            params[i + 1] = courseIds.get(i);
        }

        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }

    public long countWaitlistedByOfferingId(long offeringId) {
        String sql = """
                SELECT COUNT(*)
                FROM enrollments
                WHERE offering_id = ?
                AND enrollment_status = 'WAITLISTED'
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, offeringId);
        return count != null ? count : 0L;      //returns 0 if nothing exists
    }

    public boolean hasCompletedCourseWithMinGrade(Long studentId, Long courseId, Double minGrade) {
        String sql = """
            SELECT COUNT(*)
            FROM enrollments e
            JOIN course_offering co ON e.offering_id = co.offering_id
            LEFT JOIN grade_weights gw ON e.grade = gw.grade_letter
            WHERE e.student_id = ?
              AND co.course_id = ?
              AND e.enrollment_status = 'COMPLETED'
              AND (? IS NULL OR gw.weight IS NOT NULL AND gw.weight >= ?)
            """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, studentId, courseId, minGrade, minGrade);
        return count != null && count > 0;
    }

    // JPA: findByStudent_StudentIdAndCourseOffering_OfferingIdAndEnrollmentStatusIn(...)
    public Optional<Enrollment> findByStudentIdAndOfferingIdAndStatusIn(
            Long studentId,
            Long offeringId,
            List<Enrollment.EnrollmentStatus> statuses
    ) {
        if (statuses == null || statuses.isEmpty()) {
            return Optional.empty();
        }

        String inClause = makePlaceholders(statuses.size());
        String sql = BASE_SELECT + """
            WHERE student_id = ?
              AND offering_id = ?
              AND enrollment_status IN (%s)
            """.formatted(inClause);

        Object[] params = new Object[2 + statuses.size()];
        params[0] = studentId;
        params[1] = offeringId;
        for (int i = 0; i < statuses.size(); i++) {
            params[2 + i] = statuses.get(i).name();
        }

        List<Enrollment> result = jdbcTemplate.query(sql, rowMapper, params);
        return result.stream().findFirst();
    }

    // JPA: findFirstByCourseOffering_OfferingIdAndEnrollmentStatusOrderByWaitlistPositionAsc(...)
    public Optional<Enrollment> findFirstByOfferingIdAndStatusOrderByWaitlistPositionAsc(
            Long offeringId,
            EnrollmentStatus status
    ) {
        String sql = BASE_SELECT + """
            WHERE offering_id = ?
              AND enrollment_status = ?
            ORDER BY waitlist_position ASC
            LIMIT 1
            """;

        List<Enrollment> result = jdbcTemplate.query(sql, rowMapper, offeringId, status.name());
        return result.stream().findFirst();
    }

    // JPA: findByCourseOffering_OfferingIdAndEnrollmentStatusAndWaitlistPositionGreaterThanOrderByWaitlistPositionAsc(...)
    public List<Enrollment> findByOfferingIdAndStatusAndWaitlistPositionGreaterThanOrderByWaitlistPositionAsc(
            Long offeringId,
            EnrollmentStatus status,
            Integer waitlistPosition
    ) {
        String sql = BASE_SELECT + """
            WHERE offering_id = ?
              AND enrollment_status = ?
              AND waitlist_position > ?
            ORDER BY waitlist_position ASC
            """;

        return jdbcTemplate.query(sql, rowMapper, offeringId, status.name(), waitlistPosition);
    }

    private static String makePlaceholders(int count) {
        if (count <= 0) {
            return "";
        }
        return String.join(", ", Collections.nCopies(count, "?"));
    }


    public Optional<Enrollment> findByStudentIdAndOfferingIdWithStatuses(
            Long studentId,
            Long offeringId,
            List<Enrollment.EnrollmentStatus> statuses
    ){
        if(statuses == null || statuses.isEmpty()) {
            return Optional.empty();
        }

        String placeholders = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = """
                SELECT *
                FROM enrollments
                WHERE student_id = ?
                    AND offering_id = ?
                    AND enrollment_status IN (""" + placeholders + ")";

        List<Object> params = new ArrayList<>();
        params.add(studentId);
        params.add(offeringId);
        params.addAll(statuses.stream().map(Enum::name).toList());

        List<Enrollment> result = jdbcTemplate.query(sql, rowMapper, params.toArray());
        return result.stream().findFirst();
    }

    public Optional<Enrollment> findFirstWaitlistedByOffering(Long offeringId) {
        String sql = """
                SELECT *
                FROM enrollments
                WHERE offering_id = ?
                    AND enrollment_status = 'WAITLISTED'
                ORDER BY waitlist_position ASC
                LIMIT 1
                """;

        List<Enrollment> result = jdbcTemplate.query(sql, rowMapper, offeringId);
        return result.stream().findFirst();
    }

    public List<Enrollment> findWaitlistedWithPositionGreaterThan(Long offeringId, int position){
        String sql = """
                SELECT *
                FROM enrollments
                WHERE offering_id = ?
                    AND enrollment_status = 'WAITLISTED'
                    AND waitlist_position > ?
                ORDER BY waitlist_position ASC
                """;

        return jdbcTemplate.query(sql, rowMapper, offeringId, position);
    }



    public void updateAll(List<Enrollment> enrollments) {
        String sql = """
                UPDATE enrollments
                SET student_id = ?,
                    offering_id = ?,
                    enrolled_at = ?,
                    dropped_at = ?,
                    grade = ?,
                    last_updated = ?,
                    waitlist_position = ?,
                    credits_attempted = ?,
                    enrollment_status = ?
                WHERE enrollment_id = ?
                """;

        jdbcTemplate.batchUpdate(sql, enrollments, enrollments.size(),
                (ps, e) -> {
                    ps.setLong(1, e.getStudentId());
                    ps.setLong(2, e.getOfferingId());
                    ps.setObject(3, e.getEnrolledAt());
                    ps.setObject(4, e.getDroppedAt());
                    ps.setString(5, e.getGrade());
                    ps.setObject(6, e.getLastUpdated());
                    if (e.getWaitlistPosition() == null) {
                        ps.setObject(7, null);
                    } else {
                        ps.setInt(7, e.getWaitlistPosition());
                    }
                    ps.setInt(8, e.getCreditsAttempted());
                    ps.setString(9, e.getEnrollmentStatus().name());
                    ps.setLong(10, e.getId());
                });
    }

    public long countActiveByOfferingId(Long offeringId){
        String sql = """
                SELECT count(*)
                FROM enrollments
                WHERE offering_id = ?
                AND enrollment_status in ('ENROLLED', 'WAITLISTED')
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, offeringId);
        return count != null ? count : 0;
    }

    /*

    // Custom query to count enrollments by offering ID with specific statuses
    @Query("""
            SELECT count(e)
            FROM Enrollment e
            WHERE e.courseOffering.offeringId = :offeringId
            AND e.enrollmentStatus in ('ENROLLED', 'WAITLISTED')
            """)
    long countActiveByOfferingId(@Param("offeringId") long offeringId);


    // Custom query to count completed courses for a student given a list of course IDs
    @Query("""
            SELECT count(e)
            FROM Enrollment e
            WHERE e.student.studentId = :studentId
            AND e.enrollmentStatus = 'COMPLETED'
            AND e.courseOffering.course.courseId IN :courseIds
            """)
    long countCompletedCourses(@Param("studentId") Long studentId, @Param("courseIds") List<Long> courseIds);


    Optional<Enrollment> findByStudent_StudentIdAndCourseOffering_OfferingIdAndEnrollmentStatusIn(Long studentId, Long offeringId, List<Enrollment.EnrollmentStatus> statuses);

    Optional<Enrollment> findFirstByCourseOffering_OfferingIdAndEnrollmentStatusOrderByWaitlistPositionAsc(Long offeringId, Enrollment.EnrollmentStatus status);

    List<Enrollment> findByCourseOffering_OfferingIdAndEnrollmentStatusAndWaitlistPositionGreaterThanOrderByWaitlistPositionAsc(Long offeringId, Enrollment.EnrollmentStatus status, Integer waitlistPosition);

    Long student(Student student);
     */
}
