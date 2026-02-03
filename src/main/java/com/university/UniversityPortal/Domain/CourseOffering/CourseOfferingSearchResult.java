package com.university.UniversityPortal.Domain.CourseOffering;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CourseOfferingSearchResult {
    Long offeringId;
    Long courseId;
    String courseCode;
    String courseName;
    String semester;
    String instructor;
    String startTime;
    String endTime;
    String daysTaught;
    String dateRange;
    String delivery;
    String location;
    int seatCapacity;
    int enrolled;
    short section;
}