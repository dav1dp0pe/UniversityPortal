package com.university.UniversityPortal.Services;

import com.university.UniversityPortal.Repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final EnrollmentRepository enrollmentRepository;

    //constructor
    public RegistrationService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    //service to register for classes
    //add checks for unique student IDs, valid course capacity, and prerequisites
    public void registerForClass (Long studentId, Long classId) {
        // Implementation for registering a student for a class
        //enforce seat capacity, already enrolled, prerequisites, credit limit
        //verify student exists + active
        //verify class exists
        //prevent duplicate registrations
        //save enrollment record
        //all inside @Transactional


    }


}