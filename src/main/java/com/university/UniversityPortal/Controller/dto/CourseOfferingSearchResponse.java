package com.university.UniversityPortal.Controller.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CourseOfferingSearchResponse {
    Long offeringId;
    Long courseId;
    String courseCode;
    String courseName;
    String semester;
    String instructor;
    String startTime;
    String endTime;
    String daysTaught;
    String delivery;
    String location;
    int seatCapacity;
    int enrolled;
    short section;
}