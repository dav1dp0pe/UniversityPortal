package com.university.UniversityPortal.Repository.CourseRepository;

import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOfferingSearchResult;
import com.university.UniversityPortal.Repository.RowMappers.CourseOfferingRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseOfferingJDBCRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CourseOfferingRowMapper courseOfferingRowMapper = new CourseOfferingRowMapper();

    public CourseOfferingJDBCRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    //TODO add save, update, insert, delete methods
    // add findBy... methods

    //find the offerings corresponding with the course id provided by the user
    public Optional<CourseOffering> findByOfferingId(Long courseOfferingId){
        String sql = """
                SELECT *
                FROM course_offering
                WHERE offering_id = ?
                """;
        List<CourseOffering> result = jdbcTemplate.query(sql, courseOfferingRowMapper, courseOfferingId);
        return result.stream().findFirst();
    }

    public CourseOffering save(CourseOffering courseOffering){
        if(courseOffering.getOfferingId() == null){
            return insert(courseOffering);
        } else{
            update(courseOffering);
            return courseOffering;
        }
    }

    public List<CourseOfferingSearchResult> findOfferingsByTermAndCoursePrefix(String semester, String courseCodePrefix){
        String sql = """
                SELECT co.offering_id,
                       co.course_id,
                       co.semester,
                       co.instructor,
                       co.start_time,
                       co.end_time,
                       co.days_taught,
                       co.date_range,
                       co.delivery,
                       co.location,
                       co.seat_capacity,
                       co.enrolled,
                       co.section,
                       c.course_code,
                       c.course_name
                FROM course_offering co
                JOIN courses c ON c.course_id = co.course_id
                WHERE co.semester = ?
                  AND c.course_code LIKE ?
                ORDER BY c.course_code, co.section
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> CourseOfferingSearchResult.builder()
                        .offeringId(rs.getLong("offering_id"))
                        .courseId(rs.getLong("course_id"))
                        .semester(rs.getString("semester"))
                        .instructor(rs.getString("instructor"))
                        .startTime(rs.getString("start_time"))
                        .endTime(rs.getString("end_time"))
                        .daysTaught(rs.getString("days_taught"))
                        .dateRange(rs.getString("date_range"))
                        .delivery(rs.getString("delivery"))
                        .location(rs.getString("location"))
                        .seatCapacity(rs.getInt("seat_capacity"))
                        .enrolled(rs.getInt("enrolled"))
                        .section(rs.getShort("section"))
                        .courseCode(rs.getString("course_code"))
                        .courseName(rs.getString("course_name"))
                        .build(),
                    semester, courseCodePrefix
        );

            // You can also set course code and name if needed
            // offering.setCourseCode(rs.getString("course_code"));
            // offering.setCourseName(rs.getString("course_name"));
    }

    public CourseOffering insert(CourseOffering courseOffering){
        String sql = """
                INSERT INTO course_offering
                    (course_id,
                     semester,
                     instructor,
                     start_time,
                     end_time,
                     days_taught,
                     date_range,
                     delivery,
                     location,
                     seat_capacity,
                     enrolled,
                     section
                    )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                    ps.setLong(1, courseOffering.getCourseId());
                    ps.setString(2, courseOffering.getSemester());
                    ps.setString(3, courseOffering.getInstructor());
                    ps.setString(4, courseOffering.getStartTime());
                    ps.setString(5, courseOffering.getEndTime());
                    ps.setString(6, courseOffering.getDaysTaught());
                    ps.setString(7, courseOffering.getDateRange());
                    ps.setString(8, courseOffering.getDelivery());
                    ps.setString(9, courseOffering.getLocation());
                    ps.setInt(10, courseOffering.getSeatCapacity());
                    ps.setInt(11, courseOffering.getEnrolled());
                    ps.setShort(12, courseOffering.getSection());
                    return ps;
                }, keyHolder);

        Number key = keyHolder.getKey();

        if(key != null){
            courseOffering.setOfferingId(key.longValue());
        }

        return courseOffering;
    }


    public void update(CourseOffering courseOffering){
        String sql = """
                UPDATE course_offering
                SET course_id = ?,
                    semester = ?,
                    section = ?,
                    instructor = ?,
                    start_time = ?,
                    end_time = ?,
                    days_taught = ?,
                    date_range = ?,
                    delivery = ?,
                    location = ?,
                    seat_capacity = ?,
                    enrolled = ?,
                    section = ?
                WHERE offering_id = ?
                """;

        jdbcTemplate.update(sql,
                courseOffering.getCourseId(),
                courseOffering.getSemester(),
                courseOffering.getInstructor(),
                courseOffering.getStartTime(),
                courseOffering.getEndTime(),
                courseOffering.getDaysTaught(),
                courseOffering.getDateRange(),
                courseOffering.getDelivery(),
                courseOffering.getLocation());
                courseOffering.getSeatCapacity();
                courseOffering.getEnrolled();
                courseOffering.getSection();
                courseOffering.getOfferingId();
    }
}
