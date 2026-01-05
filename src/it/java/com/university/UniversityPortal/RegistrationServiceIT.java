package com.university.UniversityPortal;

import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Repository.CourseOfferingRepository;
import com.university.UniversityPortal.Repository.CourseRepository;
import com.university.UniversityPortal.Repository.EnrollmentRepository;
import com.university.UniversityPortal.Repository.StudentRepository;
import com.university.UniversityPortal.Services.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

//@Testcontainers

@ActiveProfiles("test")
@SpringBootTest
public class RegistrationServiceIT {

/*    //TODO: verify username, password, and database name
      //TODO: readd testcontainers and Postgrescontainer later
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        //for tests only
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

*/
    @Autowired
    RegistrationService registrationService;

    @Autowired
    StudentRepository studentRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    CourseOfferingRepository courseOfferingRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    //
    @Test
    void register_for_class_createsEnrollment() {
        Student s = new Student();
        s.setFirstName("John");
        s.setLastName("Doe");
        s.setDateOfBirth(LocalDate.of(2004, 1, 1));
        s.setStatus("ACTIVE");
        s = studentRepository.save(s);

        Course c = new Course();
        c.setCourseName("Orientation to College of Informatics");
        c.setCourseCode("INF-101");
        c.setCreditHours(1);
        c = courseRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Fall 2025");
        offering.setSection((short) 1); //TODO: why do I have to cast this?
        offering.setSeatCapacity(30);
        offering = courseOfferingRepository.save(offering);

        Enrollment enrollment = registrationService.registerForClass(s.getStudentId(), offering.getOfferingId());

        //assertions to verify enrollment created correctly, status is ENROLLED, and exists in repository, and linked to correct student and offering
        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollment.getStudent().getStudentId()).isEqualTo(s.getStudentId());
        assertThat(enrollment.getCourseOffering().getOfferingId()).isEqualTo(offering.getOfferingId());
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);

        assertThat(enrollmentRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(
                s.getStudentId(), offering.getOfferingId()
        )).isTrue();

    }

    @Test
    void register_for_class_whenFull_assignsWaitlist() {
        Student s = new Student();
        s.setFirstName("Jane");
        s.setLastName("Smith");
        s.setDateOfBirth(LocalDate.of(2003, 5, 15));
        s.setStatus("ACTIVE");
        s = studentRepository.save(s);

        Course c = new Course();
        c.setCourseName("Introduction to Programming");
        c.setCourseCode("CS-101");
        c.setCreditHours(3);
        c = courseRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Fall 2025");
        offering.setSection((short) 1);
        offering.setSeatCapacity(1); // Set seat capacity to 1 for testing
        offering = courseOfferingRepository.save(offering);

        // First student registers and takes the only seat
        Student firstStudent = new Student();
        firstStudent.setFirstName("Alice");
        firstStudent.setLastName("Johnson");
        firstStudent.setDateOfBirth(LocalDate.of(2002, 3, 10));
        firstStudent.setStatus("ACTIVE");
        firstStudent = studentRepository.save(firstStudent);
        registrationService.registerForClass(firstStudent.getStudentId(), offering.getOfferingId());

        // Now register the second student who should be waitlisted
        Enrollment enrollment = registrationService.registerForClass(s.getStudentId(), offering.getOfferingId());

        // Assertions to verify waitlist status and position
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(enrollment.getWaitlistPosition()).isEqualTo(1);
    }

    @Test
    void register_for_class_withoutPrerequisite_throwsException() {
        Student s = new Student();
        s.setFirstName("Bob");
        s.setLastName("Brown");
        s.setDateOfBirth(LocalDate.of(2001, 7, 20));
        s.setStatus("ACTIVE");
        s = studentRepository.save(s);

        Course prereqCourse = new Course();
        prereqCourse.setCourseName("Basic Mathematics");
        prereqCourse.setCourseCode("MATH-100");
        prereqCourse.setCreditHours(3);
        prereqCourse = courseRepository.save(prereqCourse);

        Course mainCourse = new Course();
        mainCourse.setCourseName("Advanced Mathematics");
        mainCourse.setCourseCode("MATH-200");
        mainCourse.setCreditHours(3);
        mainCourse.setPrerequisites(List.of(prereqCourse));
        mainCourse = courseRepository.save(mainCourse);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(mainCourse);
        offering.setTerm("Spring 2026");
        offering.setSection((short) 1);
        offering.setSeatCapacity(30);
        offering = courseOfferingRepository.save(offering);

      /*  try {
            registrationService.registerForClass(s.getStudentId(), offering.getOfferingId());
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("has not completed prerequisite");
        }

       */

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentRepository.count();
        // 5) attempt registration -> should fail
        assertThatThrownBy(() ->
                registrationService.registerForClass(studentId, offeringId)
        ).isInstanceOf(RuntimeException.class); // or your custom exception type

        // 6) verify nothing was created
        assertThat(enrollmentRepository.count()).isEqualTo(beforeEnrollments);
        assertThat(enrollmentRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(
                studentId, offeringId
        )).isFalse();
    }

}
