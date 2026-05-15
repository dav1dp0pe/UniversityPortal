package com.university.UniversityPortal.feature.enrollment.repository;

import com.university.UniversityPortal.feature.enrollment.entity.Enrollment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class EnrollmentRowMapper implements RowMapper<Enrollment> {


    @Override
    public Enrollment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Enrollment enrollment = new Enrollment();

        enrollment.setId(rs.getLong("enrollment_id"));
        if(rs.wasNull()){
            enrollment.setId(null);
        }

        enrollment.setStudentId(rs.getLong("student_id"));
        if (rs.wasNull()) {
            enrollment.setStudentId(null);
        }

        enrollment.setOfferingId(rs.getLong("offering_id"));
        if (rs.wasNull()) {
            enrollment.setOfferingId(null);
        }

        Timestamp enrolledTs = rs.getTimestamp("enrolled_at");
        enrollment.setEnrolledAt(enrolledTs != null ? enrolledTs.toLocalDateTime() : null);

        Timestamp droppedTs = rs.getTimestamp("dropped_at");
        enrollment.setDroppedAt(droppedTs != null ? droppedTs.toLocalDateTime() : null);

        Timestamp updatedTs = rs.getTimestamp("last_updated");
        enrollment.setLastUpdated(updatedTs != null ? updatedTs.toLocalDateTime() : null);

        enrollment.setGrade(rs.getString("grade"));

        int pos = rs.getInt("waitlist_position");
        enrollment.setWaitlistPosition(rs.wasNull() ? null : pos);

        enrollment.setCreditsAttempted(rs.getInt("credits_attempted"));

        String status = rs.getString("enrollment_status");
        enrollment.setEnrollmentStatus(
                status != null ? Enrollment.EnrollmentStatus.valueOf(status) : null
        );

        return enrollment;
    }

}
