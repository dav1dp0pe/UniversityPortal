package com.university.UniversityPortal.Repository;

import com.university.UniversityPortal.Domain.Course.Course;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseRowMapper implements RowMapper<Course> {
    @Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getLong("course_id"));
        course.setCourseName(rs.getString("course_name"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCreditHours(rs.getInt("credit_hours"));
        return course;
    }
}
