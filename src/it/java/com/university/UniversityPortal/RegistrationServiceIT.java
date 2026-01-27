package com.university.UniversityPortal;

import com.university.UniversityPortal.Controller.dto.BatchRegistrationResult;
import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.Course.CoursePrerequisites;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Domain.StudentHold.HoldType;
import com.university.UniversityPortal.Domain.StudentHold.StudentHold;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Repository.*;
import com.university.UniversityPortal.Services.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

//@Testcontainers

@ActiveProfiles("test")
@SpringBootTest
@Transactional
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
    JdbcTemplate jdbcTemplate;
    @Autowired
    RegistrationService registrationService;
    @Autowired
    StudentJDBCRepository studentJDBCRepository;
    @Autowired
    CourseJDBCRepository courseJDBCRepository;
    @Autowired
    CourseOfferingJDBCRepository courseOfferingJDBCRepository;
    @Autowired
    EnrollmentJDBCRepository enrollmentJDBCRepository;
    @Autowired
    StudentHoldJDBCRepository studentHoldJDBCRepository;
    @Autowired
    private WishlistJDBCRepository wishlistJDBCRepository;
    @Autowired
    CoursePrerequisiteJDBCRepository coursePrerequisiteJDBCRepository;

    //

    @Test
    void canSeeTables() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables", Integer.class);
        System.out.println("tables=" + count);
    }

    @Test
    void studentsTableExists(){
        Integer n = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE upper(table_schema) = 'PUBLIC'
                        AND upper(table_name) = 'STUDENTS'
                        """, Integer.class);
        System.out.println("student tables found = " + n);
    }

    @Test
    void listAllTables() {
        jdbcTemplate.query("""
            select table_schema, table_name
            from information_schema.tables
            where table_type = 'TABLE'
            """, (RowCallbackHandler) rs ->
                System.out.println(rs.getString("table_schema") + "." + rs.getString("table_name"))
        );
    }
    @Test
    void register_for_class_createsEnrollment() {
        Student s = new Student();
        s.setFirstName("John");
        s.setLastName("Doe");
        s.setDateOfBirth(LocalDate.of(2004, 1, 1));
        s.setStatus("ACTIVE");
        s.setEmail("doej1@example.edu");
        s = studentJDBCRepository.save(s);

        Course c = new Course();
        c.setCourseName("Orientation to College of Informatics");
        c.setCourseCode("INF-101");
        c.setCreditHours(1);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourseId(c.getCourseId());
        offering.setSemester("Fall 2025");
        offering.setSection((short) 1); //TODO: why do I have to cast this?
        offering.setSeatCapacity(30);
        offering = courseOfferingJDBCRepository.save(offering);

        Enrollment enrollment = registrationService.registerForClass(s.getStudentId(), offering.getOfferingId());

        //assertions to verify enrollment created correctly, status is ENROLLED, and exists in repository, and linked to correct student and offering
        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollment.getStudentId()).isEqualTo(s.getStudentId());
        assertThat(enrollment.getOfferingId()).isEqualTo(offering.getOfferingId());
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);

        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering.getOfferingId())).isTrue();

    }

    @Test
    void register_for_class_whenFull_assignsWaitlist() {
        Student s = new Student();
        s.setFirstName("Jane");
        s.setLastName("Smith");
        s.setDateOfBirth(LocalDate.of(2003, 5, 15));
        s.setStatus("ACTIVE");
        s.setEmail("smithj1@example.edu");
        s = studentJDBCRepository.save(s);

        Course c = new Course();
        c.setCourseName("Introduction to Programming");
        c.setCourseCode("CS-101");
        c.setCreditHours(3);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourseId(c.getCourseId());
        offering.setSemester("Fall 2025");
        offering.setSection((short) 1);
        offering.setSeatCapacity(1); // Set seat capacity to 1 for testing
        offering = courseOfferingJDBCRepository.save(offering);

        // First student registers and takes the only seat
        Student firstStudent = new Student();
        firstStudent.setFirstName("Alice");
        firstStudent.setLastName("Johnson");
        firstStudent.setDateOfBirth(LocalDate.of(2002, 3, 10));
        firstStudent.setStatus("ACTIVE");
        firstStudent.setEmail("johnsa1@example.com");
        firstStudent = studentJDBCRepository.save(firstStudent);
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
        s.setEmail("brownb1@example.com");
        s = studentJDBCRepository.save(s);

        Course prereqCourse = new Course();
        prereqCourse.setCourseName("Basic Mathematics");
        prereqCourse.setCourseCode("MATH-100");
        prereqCourse.setCreditHours(3);
        prereqCourse = courseJDBCRepository.save(prereqCourse);

        Course mainCourse = new Course();
        mainCourse.setCourseName("Advanced Mathematics");
        mainCourse.setCourseCode("MATH-200");
        mainCourse.setCreditHours(3);
        mainCourse = courseJDBCRepository.save(mainCourse);

        CoursePrerequisites prerequisite = CoursePrerequisites.builder()
                .courseId(mainCourse.getCourseId())
                .requiredCourseId(prereqCourse.getCourseId())
                .prerequisiteType(CoursePrerequisites.PrerequisteType.GRADE)
                .minGradeValue(1.7)
                .groupId(1)
                .build();
        coursePrerequisiteJDBCRepository.save(prerequisite);

        CourseOffering offering = new CourseOffering();
        offering.setCourseId(mainCourse.getCourseId());
        offering.setSemester("Spring 2026");
        offering.setSection((short) 1);
        offering.setSeatCapacity(30);
        offering = courseOfferingJDBCRepository.save(offering);

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentJDBCRepository.countActiveByOfferingId(offeringId);

        try {
            registrationService.registerForClass(s.getStudentId(), offering.getOfferingId());
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("has not completed prerequisite");
        }




        // 5) attempt registration -> should fail
        assertThatThrownBy(() ->
                registrationService.registerForClass(studentId, offeringId)
        ).isInstanceOf(RuntimeException.class); // or your custom exception type

        // 6) verify nothing was created
        assertThat(enrollmentJDBCRepository.countActiveByOfferingId(offeringId)).isEqualTo(beforeEnrollments);
        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(
                studentId, offeringId
        )).isFalse();
    }

    /*
    @Test
    void dropClass_whenNotRegistered_throwsException() {
        Student s = new Student();
        s.setFirstName("Tom");
        s.setLastName("Hanks");
        s.setDateOfBirth(LocalDate.of(2000, 12, 25));
        s.setStatus("ACTIVE");
        s = studentJDBCRepository.save(s);

        Course c = new Course();
        c.setCourseName("History 101");
        c.setCourseCode("HIST-101");
        c.setCreditHours(3);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Fall 2025");
        offering.setSection((short) 1);
        offering.setSeatCapacity(1);
        offering = courseOfferingJDBCRepository.save(offering);

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentJDBCRepository.count();

        assertThatThrownBy(() ->
                registrationService.dropClass(studentId, offeringId)
        ).isInstanceOf(RuntimeException.class);

        assertThat(enrollmentJDBCRepository.count()).isEqualTo(beforeEnrollments);
    }

    //TODO, what if someone is behind the waitlisted person? They should be promoted up
    @Test
    void dropClass_whenWaitlisted_setsDropped_doesNotPromoteAnyone() {
        Course c = new Course();
        c.setCourseName("Physics 101");
        c.setCourseCode("PHYS-101");
        c.setCreditHours(4);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Spring 2026");
        offering.setSection((short) 1);
        offering.setSeatCapacity(1);
        offering = courseOfferingJDBCRepository.save(offering);

        //Student A - will be enrolled
        Student studentA = new Student();
        studentA.setFirstName("Student");
        studentA.setLastName("A");
        studentA.setDateOfBirth(LocalDate.of(2001, 1, 1));
        studentA.setStatus("ACTIVE");
        studentA = studentJDBCRepository.save(studentA);

        //Student B - will be waitlisted
        Student studentB = new Student();
        studentB.setFirstName("Student");
        studentB.setLastName("B");
        studentB.setDateOfBirth(LocalDate.of(2002, 2, 2));
        studentB.setStatus("ACTIVE");
        studentB = studentJDBCRepository.save(studentB);

        long offeringId = offering.getOfferingId();

        Enrollment e1 = registrationService.registerForClass(studentA.getStudentId(), offeringId);
        Enrollment e2 = registrationService.registerForClass(studentB.getStudentId(), offeringId);

        assertThat(e1.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
        assertThat(e2.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);

        //drop waitlisted student B
        Enrollment dropped = registrationService.dropClass(studentB.getStudentId(), offeringId);

        assertThat(dropped.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.DROPPED);

        //enrolled student A should remain enrolled
        Enrollment checkA = enrollmentJDBCRepository.findById(e1.getId()).orElseThrow();
        assertThat(checkA.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
    }

    @Test
    void dropClass_whenEnrolled_promoteNextWaitlistedStudent(){
        Course c = new Course();
        c.setCourseName("Chemistry 101");
        c.setCourseCode("CHEM-101");
        c.setCreditHours(4);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Spring 2026");
        offering.setSection((short) 1);
        offering.setSeatCapacity(1);
        offering = courseOfferingJDBCRepository.save(offering);

        //Student A - will be enrolled
        Student studentA = new Student();
        studentA.setFirstName("Student");
        studentA.setLastName("A");
        studentA.setDateOfBirth(LocalDate.of(2001, 1, 1));
        studentA.setStatus("ACTIVE");
        studentA = studentJDBCRepository.save(studentA);

        //Student B - will be waitlisted
        Student studentB = new Student();
        studentB.setFirstName("Student");
        studentB.setLastName("B");
        studentB.setDateOfBirth(LocalDate.of(2002, 2, 2));
        studentB.setStatus("ACTIVE");
        studentB = studentJDBCRepository.save(studentB);

        long offeringId = offering.getOfferingId();

        Enrollment enrolled = registrationService.registerForClass(studentA.getStudentId(), offeringId);
        Enrollment waitlisted = registrationService.registerForClass(studentB.getStudentId(), offeringId);

        assertThat(enrolled.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
        assertThat(waitlisted.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);

        //drop enrolled student A
        Enrollment dropped = registrationService.dropClass(studentA.getStudentId(), offeringId);

        assertThat(dropped.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.DROPPED);

        //waitlisted student B should be promoted to enrolled
        Enrollment checkB = enrollmentJDBCRepository.findById(waitlisted.getId()).orElseThrow();
        assertThat(checkB.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
    }

    @Test
    void dropClass_whenWaitlisted_promoteNextWaitlistedStudent(){
        Course c = new Course();
        c.setCourseName("Biology 101");
        c.setCourseCode("BIO-101");
        c.setCreditHours(4);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Spring 2026");
        offering.setSection((short) 1);
        offering.setSeatCapacity(1);
        offering = courseOfferingJDBCRepository.save(offering);

        //Student A - will be enrolled
        Student studentA = new Student();
        studentA.setFirstName("Student");
        studentA.setLastName("A");
        studentA.setDateOfBirth(LocalDate.of(2001, 1, 1));
        studentA.setStatus("ACTIVE");
        studentA = studentJDBCRepository.save(studentA);

        //Student B - will be waitlisted
        Student studentB = new Student();
        studentB.setFirstName("Student");
        studentB.setLastName("B");
        studentB.setDateOfBirth(LocalDate.of(2002, 2, 2));
        studentB.setStatus("ACTIVE");
        studentB = studentJDBCRepository.save(studentB);

        //Student C - will be waitlisted second
        Student studentC = new Student();
        studentC.setFirstName("Student");
        studentC.setLastName("C");
        studentC.setDateOfBirth(LocalDate.of(2003, 3, 3));
        studentC.setStatus("ACTIVE");
        studentC = studentJDBCRepository.save(studentC);

        long offeringId = offering.getOfferingId();

        Enrollment enrolled = registrationService.registerForClass(studentA.getStudentId(), offeringId);
        Enrollment waitlisted1 = registrationService.registerForClass(studentB.getStudentId(), offeringId);
        Enrollment waitlisted2 = registrationService.registerForClass(studentC.getStudentId(), offeringId);

        //verify initial statuses
        Enrollment checkCBeforeDropC = enrollmentJDBCRepository.findById(waitlisted2.getId()).orElseThrow();

        //assert that initial statuses are correct
        assertThat(enrolled.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
        assertThat(waitlisted1.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(waitlisted2.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(checkCBeforeDropC.getWaitlistPosition()).isEqualTo(2);


        //drop waitlisted student B
        Enrollment dropped = registrationService.dropClass(studentB.getStudentId(), offeringId);
        assertThat(dropped.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.DROPPED);

        //student c should be promoted in waitlist position
        Enrollment checkAfterDropC = enrollmentJDBCRepository.findById(waitlisted2.getId()).orElseThrow();
        assertThat(checkAfterDropC.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(checkAfterDropC.getWaitlistPosition()).isEqualTo(1);
    }

    @Test
    void registerClass_advisingHold_throwsException() {
        Student s = new Student();
        s.setFirstName("Hold");
        s.setLastName("Student");
        s.setDateOfBirth(LocalDate.of(2000, 6, 15));
        s.setStatus("ACTIVE");

        s = studentJDBCRepository.save(s);

        //put an advising hold on the student
        StudentHold hold = new StudentHold();
        hold.setStudent(s);
        hold.setHoldType(HoldType.ADVISING);
        hold.setReason("Advising hold prevents registration until advisor approval.");
        hold.setActive(true);
        hold.setPlacedAt(LocalDateTime.now());
        studentHoldRepository.save(hold);

        Course c = new Course();
        c.setCourseName("Philosophy 101");
        c.setCourseCode("PHIL-101");
        c.setCreditHours(3);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Fall 2025");
        offering.setSection((short) 1);
        offering.setSeatCapacity(30);
        offering = courseOfferingJDBCRepository.save(offering);

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentJDBCRepository.count();

        assertThatThrownBy(() ->
                registrationService.registerForClass(studentId, offeringId)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Student has an active hold and cannot register.");

        assertThat(enrollmentJDBCRepository.count()).isEqualTo(beforeEnrollments);
        assertThat(enrollmentJDBCRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(
                studentId, offeringId
        )).isFalse();
    }

    @Test
    void addClass_toWishlist_succeeds() {
        Student s = new Student();
        s.setFirstName("Wish");
        s.setLastName("Lister");
        s.setDateOfBirth(LocalDate.of(1999, 11, 11));
        s.setStatus("ACTIVE");
        s = studentJDBCRepository.save(s);

        Course c = new Course();
        c.setCourseName("Art History 101");
        c.setCourseCode("ART-101");
        c.setCreditHours(3);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Fall 2025");
        offering.setSection((short) 1);
        offering.setSeatCapacity(30);
        offering = courseOfferingJDBCRepository.save(offering);

        long beforeWishlistCount = wishlistRepository.count();

        Wishlist item = registrationService.addToWishlist(s.getStudentId(), offering.getOfferingId());

        assertThat(item.getId()).isNotNull();
        assertThat(item.getStudent().getStudentId()).isEqualTo(s.getStudentId());
        assertThat(item.getCourseOffering().getOfferingId()).isEqualTo(offering.getOfferingId());

        assertThat(wishlistRepository.count()).isEqualTo(beforeWishlistCount + 1);

        assertThat(
                wishlistRepository.findByStudent_StudentIdAndCourseOffering_OfferingId(
                        s.getStudentId(), offering.getOfferingId()).isPresent()
        ).isTrue();
    }

    @Test
    void removeClass_fromWishlist_succeeds() {
        Student s = new Student();
        s.setFirstName("Remove");
        s.setLastName("Me");
        s.setDateOfBirth(LocalDate.of(1998, 8, 8));
        s.setStatus("ACTIVE");
        s = studentJDBCRepository.save(s);

        Course c = new Course();
        c.setCourseName("Music Theory 101");
        c.setCourseCode("MUS-101");
        c.setCreditHours(3);
        c = courseJDBCRepository.save(c);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(c);
        offering.setTerm("Fall 2025");
        offering.setSection((short) 1);
        offering.setSeatCapacity(30);
        offering = courseOfferingJDBCRepository.save(offering);

        Wishlist item = registrationService.addToWishlist(s.getStudentId(), offering.getOfferingId());

        assertThat(
                wishlistRepository.findByStudent_StudentIdAndCourseOffering_OfferingId(
                        s.getStudentId(), offering.getOfferingId()).isPresent()
        ).isTrue();

        long beforeCount = wishlistRepository.count();

        registrationService.removeFromWishlist(s.getStudentId(), offering.getOfferingId());

        assertThat(wishlistRepository.count()).isEqualTo(beforeCount - 1);
        assertThat(
                wishlistRepository.findByStudent_StudentIdAndCourseOffering_OfferingId(
                        s.getStudentId(), offering.getOfferingId()).isPresent()
        ).isFalse();
    }

    @Test
    void registerAllFromWishlist_registersAllOfferings(){
        Student s = new Student();
        s.setFirstName("Batch");
        s.setLastName("Register");
        s.setDateOfBirth(LocalDate.of(1997, 7, 7));
        s.setStatus("ACTIVE");
        s = studentJDBCRepository.save(s);

        Course c1 = new Course();
        c1.setCourseName("Economics 101");
        c1.setCourseCode("ECON-101");
        c1.setCreditHours(3);
        c1 = courseJDBCRepository.save(c1);

        CourseOffering offering1 = new CourseOffering();
        offering1.setCourse(c1);
        offering1.setTerm("Fall 2025");
        offering1.setSection((short) 1);
        offering1.setSeatCapacity(30);
        offering1 = courseOfferingJDBCRepository.save(offering1);

        Course c2 = new Course();
        c2.setCourseName("Sociology 101");
        c2.setCourseCode("SOC-101");
        c2.setCreditHours(3);
        c2 = courseJDBCRepository.save(c2);

        CourseOffering offering2 = new CourseOffering();
        offering2.setCourse(c2);
        offering2.setTerm("Fall 2025");
        offering2.setSection((short) 1);
        offering2.setSeatCapacity(30);
        offering2 = courseOfferingJDBCRepository.save(offering2);

        Course c3 = new Course();
        c3.setCourseName("Psychology 101");
        c3.setCourseCode("PSY-101");
        c3.setCreditHours(3);
        c3 = courseJDBCRepository.save(c3);

        CourseOffering offering3 = new CourseOffering();
        offering3.setCourse(c3);
        offering3.setTerm("Fall 2025");
        offering3.setSection((short) 1);
        offering3.setSeatCapacity(30);
        offering3 = courseOfferingJDBCRepository.save(offering3);

        //add all offerings to wishlist
        registrationService.addToWishlist(s.getStudentId(), offering1.getOfferingId());
        registrationService.addToWishlist(s.getStudentId(), offering2.getOfferingId());
        registrationService.addToWishlist(s.getStudentId(), offering3.getOfferingId());

        long beforeEnrollments = enrollmentJDBCRepository.count();

        //register all from wishlist
        var results = registrationService.registerAllFromWishlist(s.getStudentId(), "Fall 2025");

        assertThat(results.size()).isEqualTo(3);

        //every registration should be successful
        assertThat(results.stream().allMatch(BatchRegistrationResult::success)).isTrue();

        //verify enrollments exist for both offerings
        assertThat(enrollmentJDBCRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(
                s.getStudentId(), offering1.getOfferingId()
        )).isTrue();

        assertThat(enrollmentJDBCRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(
                s.getStudentId(), offering2.getOfferingId()
        )).isTrue();

        assertThat(enrollmentJDBCRepository.existsByStudent_StudentIdAndCourseOffering_OfferingId(
                s.getStudentId(), offering3.getOfferingId()
        )).isTrue();

        assertThat(enrollmentJDBCRepository.count()).isEqualTo(beforeEnrollments + 3);
    }
*/
    //TODO: add test for registeringAll classes when some fail (ex: full class, hold, missing prereq)


}
