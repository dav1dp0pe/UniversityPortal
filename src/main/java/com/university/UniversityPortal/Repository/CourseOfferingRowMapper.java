package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseOfferingRowMapper implements RowMapper<CourseOffering> {
    @Override
    public CourseOffering mapRow(ResultSet rs, int rowNum) throws SQLException {
        CourseOffering courseOffering = new CourseOffering();
        courseOffering.setOfferingId(rs.getLong("offering_id"));
        courseOffering.setCourseId(rs.getLong("course_id"));
        courseOffering.setSemester(rs.getString("semester"));
        courseOffering.setInstructor(rs.getString("instructor"));
        courseOffering.setStartTime(rs.getString("start_time"));
        courseOffering.setEndTime(rs.getString("end_time"));
        courseOffering.setDaysTaught(rs.getString("days_taught"));
        courseOffering.setDateRange(rs.getString("date_range"));
        courseOffering.setDelivery(rs.getString("delivery"));
        courseOffering.setLocation(rs.getString("location"));
        courseOffering.setSeatCapacity(rs.getInt("seat_capacity"));
        courseOffering.setEnrolled(rs.getInt("enrolled"));
        courseOffering.setSection(rs.getShort("section"));
        //TODO
        // what do i do about the list of enrollments?
        return courseOffering;
    }
}
