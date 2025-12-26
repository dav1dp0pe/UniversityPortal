package com.university.UniversityPortal.Services;

import com.university.UniversityPortal.Repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentServices {

    private final StudentRepository studentRepository;

    public StudentServices(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    //service to register for classes
    //add checks for unique student IDs, valid course capacity, and prerequisites
    public void registerForClass(Long studentId, Long classId) {
        // Implementation for registering a student for a class
    }

}
