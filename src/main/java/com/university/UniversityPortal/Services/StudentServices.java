package com.university.UniversityPortal.Services;

import com.university.UniversityPortal.Repository.StudentRepository.StudentJDBCRepository;
//import com.university.UniversityPortal.Repository.StudentRepository;
import org.springframework.stereotype.Service;

//TODO: remove StudentServices if not needed
@Service
public class StudentServices {

    private final StudentJDBCRepository studentJDBCRepository;

    public StudentServices(StudentJDBCRepository studentJDBCRepository) {
        this.studentJDBCRepository = studentJDBCRepository;
    }

}
