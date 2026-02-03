package com.university.UniversityPortal;

import com.university.UniversityPortal.Controller.dto.BatchRegistrationResult;
import com.university.UniversityPortal.Domain.Course.Course;
import com.university.UniversityPortal.Domain.Course.CoursePrerequisites;
import com.university.UniversityPortal.Domain.CourseOffering.CourseOffering;
import com.university.UniversityPortal.Domain.Enrollment.Enrollment;
import com.university.UniversityPortal.Domain.Student.Student;
import com.university.UniversityPortal.Domain.StudentHold.StudentHold;
import com.university.UniversityPortal.Domain.Wishlist.Wishlist;
import com.university.UniversityPortal.Repository.CourseRepository.CourseJDBCRepository;
import com.university.UniversityPortal.Repository.CourseRepository.CourseOfferingJDBCRepository;
import com.university.UniversityPortal.Repository.CourseRepository.CoursePrerequisiteJDBCRepository;
import com.university.UniversityPortal.Repository.CourseRepository.WishlistJDBCRepository;
import com.university.UniversityPortal.Repository.EnrollmentRepository.EnrollmentJDBCRepository;
import com.university.UniversityPortal.Repository.StudentRepository.StudentHoldJDBCRepository;
import com.university.UniversityPortal.Repository.StudentRepository.StudentJDBCRepository;
import com.university.UniversityPortal.Services.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        Student s = createAndSaveStudent("John", "Doe", LocalDate.of(2004, 1, 1), "ACTIVE", "doej1@example.edu");

        Course c = createAndSaveCourse("Orientation to College of Informatics", "INF-101", 1);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Fall 2025", (short) 1,30);

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
        Student s = createAndSaveStudent("Jane", "Smith", LocalDate.of(2003, 5, 15), "ACTIVE", "smithj1@example.edu");

        Course c = createAndSaveCourse("Introduction to Programming", "CS-101", 3);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Fall 2025", (short) 1, 1);

        // First student registers and takes the only seat
        Student firstStudent = createAndSaveStudent("Alice", "Johnson", LocalDate.of(2002, 3, 10), "ACTIVE", "johnsa1@example.com");

        registrationService.registerForClass(firstStudent.getStudentId(), offering.getOfferingId());

        // Now register the second student who should be waitlisted
        Enrollment enrollment = registrationService.registerForClass(s.getStudentId(), offering.getOfferingId());

        // Assertions to verify waitlist status and position
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(enrollment.getWaitlistPosition()).isEqualTo(1);
    }


    @Test
    void register_for_class_withoutPrerequisite_throwsException() {
        Student s = createAndSaveStudent("Bob", "Brown", LocalDate.of(2001, 7, 20), "ACTIVE", "brownb1@example.com");

        Course prereqCourse = createAndSaveCourse("Basic Mathematics", "MATH-100", 3);

        Course mainCourse = createAndSaveCourse("Advanced Mathematics", "MATH-200", 3);

        CoursePrerequisites prerequisite = CoursePrerequisites.builder()
                .courseId(mainCourse.getCourseId())
                .requiredCourseId(prereqCourse.getCourseId())
                .prerequisiteType(CoursePrerequisites.PrerequisteType.GRADE)
                .minGradeValue(1.7)
                .groupId(1)
                .build();
        coursePrerequisiteJDBCRepository.save(prerequisite);

        CourseOffering offering = createAndSaveCourseOffering(mainCourse.getCourseId(), "Spring 2026", (short) 1,30);

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentJDBCRepository.countActiveByOfferingId(offeringId);

        assertThatThrownBy(() -> registrationService.registerForClass(studentId, offeringId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You have not completed all prerequisites for MATH-200");

        // 6) verify nothing was created
        assertThat(enrollmentJDBCRepository.countActiveByOfferingId(offeringId)).isEqualTo(beforeEnrollments);
        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(studentId, offeringId)).isFalse();
    }


    @Test
    void dropClass_whenNotRegistered_throwsException() {
        Student s = createAndSaveStudent("Tom", "Hanks", LocalDate.of(2000, 12, 25), "ACTIVE", "hankst1@example.com");

        Course c = createAndSaveCourse("History 101", "HIST-101", 3);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Fall 2025", (short) 1, 1);

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentJDBCRepository.countActiveByOfferingId(offeringId);;

        assertThatThrownBy(() ->
                registrationService.dropClass(studentId, offeringId)
        ).isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Student is not enrolled or waitlisted for this offering.");

        assertThat(enrollmentJDBCRepository.countActiveByOfferingId(offeringId)).isEqualTo(beforeEnrollments);

        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(studentId, offeringId)).isFalse();
    }

    //TODO, what if someone is behind the waitlisted person? They should be promoted up
    @Test
    void dropClass_whenWaitlisted_setsDropped_doesNotPromoteAnyone() {
        Course c = createAndSaveCourse("Physics 101", "PHYS-101", 4);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Spring 2026", (short) 1, 1);
        offering.setCourseId(c.getCourseId());

        //Student A - will be enrolled
        Student studentA = createAndSaveStudent("Student", "A", LocalDate.of(2001, 1, 1), "ACTIVE", "studentA@example.edu");

        //Student B - will be waitlisted
        Student studentB = createAndSaveStudent("Student", "B", LocalDate.of(2002, 2, 2), "ACTIVE", "studentB@example.edu");

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
        Course c = createAndSaveCourse("Chemistry 101", "CHEM-101", 4);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Spring 2026", (short) 1, 1);

        //Student A - will be enrolled
        Student studentA = createAndSaveStudent("Student", "A", LocalDate.of(2001, 1, 1), "ACTIVE", "studentA@example.edu");

        //Student B - will be waitlisted
        Student studentB = createAndSaveStudent("Student", "B", LocalDate.of(2002, 2, 2), "ACTIVE", "studentB@example.edu");

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
    void dropClass_whenWaitlisted_moveNextWaitlistedStudentsPositionUp(){
        Course c = createAndSaveCourse("Biology 101", "BIO-101", 4);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Spring 2026", (short) 1, 1);

        //Student A - will be enrolled
        Student studentA = createAndSaveStudent("Student", "A", LocalDate.of(2001, 1, 1), "ACTIVE", "studentA@example.com");

        //Student B - will be waitlisted
        Student studentB = createAndSaveStudent("Student", "B", LocalDate.of(2002, 2, 2), "ACTIVE", "studentB@example.edu");

        //Student C - will be waitlisted second
        Student studentC = createAndSaveStudent("Student", "C", LocalDate.of(2003, 3, 3), "ACTIVE", "studentC@example.edu");

        long offeringId = offering.getOfferingId();

        Enrollment enrolled = registrationService.registerForClass(studentA.getStudentId(), offeringId);
        Enrollment waitlisted1 = registrationService.registerForClass(studentB.getStudentId(), offeringId);
        Enrollment waitlisted2 = registrationService.registerForClass(studentC.getStudentId(), offeringId);

        //verify initial statuses
        Enrollment checkBeforeDropB = enrollmentJDBCRepository.findById(waitlisted1.getId()).orElseThrow();
        Enrollment checkCBeforeDropC = enrollmentJDBCRepository.findById(waitlisted2.getId()).orElseThrow();

        //assert that initial statuses are correct
        assertThat(enrolled.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
        assertThat(waitlisted1.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(checkBeforeDropB.getWaitlistPosition()).isEqualTo(1);
        assertThat(waitlisted2.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(checkCBeforeDropC.getWaitlistPosition()).isEqualTo(2);


        //drop waitlisted student B
        Enrollment dropped = registrationService.dropClass(waitlisted1.getStudentId(), offeringId);
        assertThat(dropped.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.DROPPED);

        //student c should be promoted in waitlist position
        Enrollment checkAfterDropC = enrollmentJDBCRepository.findById(waitlisted2.getId()).orElseThrow();
        assertThat(checkAfterDropC.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(checkAfterDropC.getWaitlistPosition()).isEqualTo(1);
    }


    @Test
    void registerClass_advisingHold_throwsException() {
        Student s = createAndSaveStudent("Hold", "Student", LocalDate.of(2000, 6, 15), "ACTIVE", "student1@example.com");

        //put an advising hold on the student
        StudentHold hold = new StudentHold();
        hold.setStudentId(s.getStudentId());
        hold.setHoldType(StudentHold.HoldType.ADVISING);
        hold.setReason("Advising hold prevents registration until advisor approval.");
        hold.setActive(true);
        hold.setPlacedAt(LocalDateTime.now());
        studentHoldJDBCRepository.save(hold);

        Course c = createAndSaveCourse("Philosophy 101", "PHIL-101", 3);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Fall 2025", (short) 1,30);

        long studentId = s.getStudentId();
        long offeringId = offering.getOfferingId();
        long beforeEnrollments = enrollmentJDBCRepository.countActiveByOfferingId(offeringId);;

        assertThatThrownBy(() ->
                registrationService.registerForClass(studentId, offeringId)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Student has an active hold and cannot register.");

        assertThat(enrollmentJDBCRepository.countActiveByOfferingId(offeringId)).isEqualTo(beforeEnrollments);
        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(studentId, offeringId)).isFalse();
    }

    @Test
    void addClass_toWishlist_succeeds() {
        Student s = createAndSaveStudent("Wish", "Lister", LocalDate.of(1999, 11, 11), "ACTIVE", "wishlist1@example.edu");

        Course c = createAndSaveCourse("Art History 101", "ART-101", 3);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Fall 2025", (short) 1, 30);

        long beforeWishlistCount = wishlistJDBCRepository.count();
        assertThat(beforeWishlistCount).isEqualTo(0);

        Wishlist item = registrationService.addToWishlist(s.getStudentId(), offering.getOfferingId());

        assertThat(item.getWishlistId()).isNotNull();
        assertThat(item.getStudentId()).isEqualTo(s.getStudentId());
        assertThat(item.getOfferingId()).isEqualTo(offering.getOfferingId());

        assertThat(wishlistJDBCRepository.count()).isEqualTo(beforeWishlistCount + 1);

        assertThat(wishlistJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering.getOfferingId())).isTrue();
    }

    @Test
    void removeClass_fromWishlist_succeeds() {
        Student s = createAndSaveStudent("Remove", "Me", LocalDate.of(1998, 8, 8), "ACTIVE", "removal@email.edu");

        Course c = createAndSaveCourse("Music Theory 101", "MUS-101", 3);

        CourseOffering offering = createAndSaveCourseOffering(c.getCourseId(), "Fall 2025", (short) 1, 30);

        long beforeCount = wishlistJDBCRepository.count();
        assertThat(beforeCount).isEqualTo(0);

        Wishlist item = registrationService.addToWishlist(s.getStudentId(), offering.getOfferingId());
        assertThat(item.getWishlistId()).isNotNull();

        assertThat(wishlistJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering.getOfferingId())).isTrue();
        assertThat(wishlistJDBCRepository.count()).isEqualTo(beforeCount + 1);

        registrationService.removeFromWishlist(s.getStudentId(), offering.getOfferingId());

        assertThat(wishlistJDBCRepository.count()).isEqualTo(beforeCount);
        assertThat(wishlistJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering.getOfferingId())).isFalse();
    }

    @Test
    void registerAllFromWishlist_registersAllOfferings(){
        Student s = createAndSaveStudent("Batch", "Register", LocalDate.of(1997, 7, 7), "ACTIVE", "batchemail@example.edu");

        Course c1 = createAndSaveCourse("Economics 101", "ECON-101", 3);
        CourseOffering offering1 = createAndSaveCourseOffering(c1.getCourseId(), "Fall 2025", (short) 1, 30);

        Course c2 = createAndSaveCourse("Sociology 101", "SOC-101", 3);
        CourseOffering offering2 = createAndSaveCourseOffering(c2.getCourseId(), "Fall 2025", (short) 1, 30);

        Course c3 = createAndSaveCourse("Psychology 101", "PSY-101", 3);
        CourseOffering offering3 = createAndSaveCourseOffering(c3.getCourseId(), "Fall 2025", (short) 1, 30);

        long beforeWishlistCount = wishlistJDBCRepository.count();
        assertThat(beforeWishlistCount).isEqualTo(0);

        //ensure no enrollments exist yet
        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering1.getOfferingId())).isFalse();
        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering2.getOfferingId())).isFalse();
        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering3.getOfferingId())).isFalse();

        //add all offerings to wishlist
        registrationService.addToWishlist(s.getStudentId(), offering1.getOfferingId());
        registrationService.addToWishlist(s.getStudentId(), offering2.getOfferingId());
        registrationService.addToWishlist(s.getStudentId(), offering3.getOfferingId());

        assertThat(wishlistJDBCRepository.count()).isEqualTo(beforeWishlistCount + 3);

        //register all from wishlist
        var results = registrationService.registerAllFromWishlist(s.getStudentId(), "Fall 2025");

        assertThat(results.size()).isEqualTo(3);

        //every registration should be successful
        assertThat(results.stream().allMatch(BatchRegistrationResult::success)).isTrue();

        //verify enrollments exist for all offerings

        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering1.getOfferingId())).isTrue();

        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering2.getOfferingId())).isTrue();

        assertThat(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(s.getStudentId(), offering3.getOfferingId())).isTrue();

        //verify there are 3 new enrollments
        long beforeEnrollments = enrollmentJDBCRepository.count() - 3;
        assertThat(enrollmentJDBCRepository.count()).isEqualTo(beforeEnrollments + 3);
    }

    //TODO implement these tests
    @Test
    void registerAllFromWishlist_1CourseFails_othersSucceed(){
        Student s = createAndSaveStudent("Partial", "Success", LocalDate.of(1996, 6, 6), "ACTIVE", "partial1@example.edu");

        Course c1 = createAndSaveCourse("Anthropology 101", "ANTH-101", 3);
        CourseOffering offering1 = createAndSaveCourseOffering(c1.getCourseId(), "Fall 2025", (short) 1, 30);

        Course c2 = createAndSaveCourse("Geography 101", "GEO-101", 3);
        CourseOffering offering2 = createAndSaveCourseOffering(c2.getCourseId(), "Fall 2025", (short) 1, 0); //full course to cause failure

        Course c3 = createAndSaveCourse("Political Science 101", "POL-101", 3);
        CourseOffering offering3 = createAndSaveCourseOffering(c3.getCourseId(), "Fall 2025", (short) 1, 30);



    }

    @Test
    void registerForClass_withTimeConflict_throwsException() {
        //TODO implement this test
    }

    @Test
    void registerForClass_exceedsCreditLimit_throwsException() {
        //TODO implement this test
    }

    @Test
    void registerForClass_withMultiplePrerequisiteGroups_succeedsIfOneGroupMet() {
        //TODO implement this test (CIT 245, OR CIT 251 prerequisite example)
    }

    @Test
    void registerForClass_withMultiplePrerequisiteGroups_success() {
        //TODO implement this test (CIT 245, OR CIT 251 prerequisite example)
    }

    @Test
    void registerForClass_withoutProperMajor_throwsException() {
        //TODO implement this test
    }

    @Test
    void registerForClass_withProperMajor_succeeds() {
        //TODO implement this test
    }

    @Test
    void registerForClass_withGPAPrerequisite_succeeds() {
        //TODO implement this test
    }

    @Test
    void registerForClass_withoutGPAPrerequisite_throwsException() {
        //TODO implement this test
    }

    @Test
    void registerForClass_withStandingPrerequisite_succeeds() {
        //TODO implement this test
    }

    @Test
    void registerForClass_withoutStandingPrerequisite_throwsException() {

    }

    @Test
    void searchCourseOfferings_bySemesterAndCourseCode_returnsResults() {
        Course c1 = createAndSaveCourse("Calculus I", "MAT-101", 4);
        Course c2 = createAndSaveCourse("Calculus II", "MAT-102", 4);

        CourseOffering offering1 = createAndSaveCourseOffering(c1.getCourseId(), "Fall 2025", (short) 1, 30);
        CourseOffering offering2 = createAndSaveCourseOffering(c2.getCourseId(), "Fall 2025", (short) 1, 30);
        CourseOffering offering3 = createAndSaveCourseOffering(c1.getCourseId(), "Spring 2026", (short) 1, 30);

        var results = registrationService.searchOfferingsBySemesterAndCourseCode("Fall 2025", "MAT-101");

        System.out.println(results);

        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getCourseCode()).isEqualTo("MAT-101");
        assertThat(results.get(0).getSemester()).isEqualTo("Fall 2025");
    }

    @Test
    void searchCourseOfferings_bySemesterAndDepartment_returnsResults() {
        Course c1 = createAndSaveCourse("Calculus I", "MAT-101", 4);
        Course c2 = createAndSaveCourse("Calculus II", "MAT-102", 4);

        CourseOffering offering1 = createAndSaveCourseOffering(c1.getCourseId(), "Fall 2025", (short) 1, 30);
        CourseOffering offering2 = createAndSaveCourseOffering(c2.getCourseId(), "Fall 2025", (short) 1, 30);
        CourseOffering offering3 = createAndSaveCourseOffering(c1.getCourseId(), "Spring 2026", (short) 1, 30);

        var results = registrationService.searchOfferingsBySemesterAndCourseCode("Fall 2025", "MAT");

        System.out.println(results);

        assertThat(results.size()).isEqualTo(2);
        assertThat(results.get(0).getCourseCode()).isEqualTo("MAT-101");
        assertThat(results.get(1).getCourseCode()).isEqualTo("MAT-102");
        assertThat(results.get(0).getSemester()).isEqualTo("Fall 2025");
    }


    private Student createAndSaveStudent(String firstName, String lastName, LocalDate dob, String status, String email) {
        Student s = new Student();
        s.setFirstName(firstName);
        s.setLastName(lastName);
        s.setDateOfBirth(dob);
        s.setStatus(status);
        s.setEmail(email);
        return studentJDBCRepository.save(s);
    }

    private Course createAndSaveCourse(String courseName, String courseCode, int creditHours) {
        Course c = new Course();
        c.setCourseName(courseName);
        c.setCourseCode(courseCode);
        c.setCreditHours(creditHours);
        return courseJDBCRepository.save(c);
    }

    private CourseOffering createAndSaveCourseOffering(long courseId, String semester, short section, int seatCapacity) {
        CourseOffering offering = new CourseOffering();
        offering.setCourseId(courseId);
        offering.setSemester(semester);
        offering.setSection(section);
        offering.setSeatCapacity(seatCapacity);
        return courseOfferingJDBCRepository.save(offering);
    }

}
