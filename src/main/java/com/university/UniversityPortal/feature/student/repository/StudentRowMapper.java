package com.university.UniversityPortal.feature.student.repository;


import com.university.UniversityPortal.feature.program.entity.AcademicProgram;
import com.university.UniversityPortal.feature.student.entity.Student;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentRowMapper implements RowMapper<Student> {

    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        Student student = new Student();

        student.setStudentId(rs.getLong("student_id"));

        //
        if(rs.wasNull()){
            student.setStudentId(null);
        }

        student.setFirstName(rs.getString("first_name"));
        student.setMiddleName(rs.getString("middle_name"));
        student.setLastName(rs.getString("last_name"));

        java.sql.Date sqlDate = rs.getDate("date_of_birth");
        student.setDateOfBirth(sqlDate != null ? sqlDate.toLocalDate() : null);

        student.setStatus(rs.getString("status"));
        student.setEmail(rs.getString("email"));
        student.setPhoneNumber(rs.getString("phone_number"));
        student.setAddress(rs.getString("address"));

        student.setGpa(rs.getFloat("gpa"));
        if(rs.wasNull()){
            student.setGpa(0.0f);
        }

        student.setCreditsCompleted(rs.getInt("credits_completed"));
        student.setCreditsAttempted(rs.getInt("credits_attempted"));

        long majorId = rs.getLong("major_id");
        if(!rs.wasNull()){
            AcademicProgram major = new AcademicProgram();
            major.setMajorId(majorId);
            student.setMajor(major);
        }

        long minorId = rs.getLong("minor_id");
        if(!rs.wasNull()){
            AcademicProgram minor = new AcademicProgram();
            minor.setMajorId(minorId);
            student.setMinor(minor);
        }

        return student;
    }
}
