package com.university.UniversityPortal.feature.courseoffering.repository;

import com.university.UniversityPortal.feature.courseoffering.entity.CourseOffering;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalTime;
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
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"offering_id"});

                    ps.setLong(1, courseOffering.getCourseId());
                    ps.setString(2, courseOffering.getSemester());
                    ps.setString(3, courseOffering.getInstructor());
                    // TIME columns must use setNull(Types.TIME) or setObject(LocalTime) — setString sends
                    // VARCHAR type which Postgres rejects even for null values
                    if (courseOffering.getStartTime() != null) {
                        ps.setObject(4, LocalTime.parse(courseOffering.getStartTime()));
                    } else {
                        ps.setNull(4, Types.TIME);
                    }
                    if (courseOffering.getEndTime() != null) {
                        ps.setObject(5, LocalTime.parse(courseOffering.getEndTime()));
                    } else {
                        ps.setNull(5, Types.TIME);
                    }
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
                courseOffering.getLocation(),
                courseOffering.getSeatCapacity(),
                courseOffering.getEnrolled(),
                courseOffering.getSection(),
                courseOffering.getOfferingId());
    }
}
