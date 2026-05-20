package com.university.UniversityPortal.feature.registration.service;

import com.university.UniversityPortal.feature.course.entity.Course;
import com.university.UniversityPortal.feature.course.repository.CourseJDBCRepository;
import com.university.UniversityPortal.feature.course.repository.CoursePrerequisiteJDBCRepository;
import com.university.UniversityPortal.feature.courseoffering.entity.CourseOffering;
import com.university.UniversityPortal.feature.courseoffering.repository.CourseOfferingJDBCRepository;
import com.university.UniversityPortal.feature.enrollment.entity.Enrollment;
import com.university.UniversityPortal.feature.enrollment.repository.EnrollmentJDBCRepository;
import com.university.UniversityPortal.feature.student.entity.Student;
import com.university.UniversityPortal.feature.student.repository.StudentJDBCRepository;
import com.university.UniversityPortal.feature.studenthold.repository.StudentHoldJDBCRepository;
import com.university.UniversityPortal.feature.wishlist.repository.WishlistJDBCRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for RegistrationService — no Spring context, no database.
 * All repository dependencies are mocked with Mockito.
 * Runs in milliseconds and tests business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationServiceTest {

    // Mocks — Mockito creates a fake implementation of each repository
    @Mock private EnrollmentJDBCRepository enrollmentJDBCRepository;
    @Mock private StudentJDBCRepository studentJDBCRepository;
    @Mock private CourseOfferingJDBCRepository courseOfferingJDBCRepository;
    @Mock private StudentHoldJDBCRepository studentHoldJDBCRepository;
    @Mock private WishlistJDBCRepository wishlistJDBCRepository;
    @Mock private CourseJDBCRepository courseJDBCRepository;
    @Mock private CoursePrerequisiteJDBCRepository coursePrerequisiteJDBCRepository;

    // The real class under test — Mockito injects the mocks above via constructor
    @InjectMocks
    private RegistrationService registrationService;

    // --- Test data helpers ---

    private Student aStudent(long id) {
        Student s = new Student();
        s.setStudentId(id);
        s.setFirstName("Jane");
        s.setLastName("Doe");
        s.setStatus("ACTIVE");
        return s;
    }

    private CourseOffering anOffering(long offeringId, long courseId, int seatCapacity) {
        CourseOffering o = new CourseOffering();
        o.setOfferingId(offeringId);
        o.setCourseId(courseId);
        o.setSemester("Fall 2025");
        o.setSeatCapacity(seatCapacity);
        return o;
    }

    private Course aCourse(long courseId) {
        Course c = new Course();
        c.setCourseId(courseId);
        c.setCourseCode("CS-101");
        c.setCourseName("Intro to CS");
        c.setCreditHours(3);
        return c;
    }

    // --- Tests ---

    @Test
    void registerForClass_whenStudentHasActiveHold_throwsException() {
        // Arrange
        long studentId = 1L, offeringId = 10L;
        when(studentJDBCRepository.findStudentById(studentId))
                .thenReturn(Optional.of(aStudent(studentId)));
        when(studentHoldJDBCRepository.existsActiveByStudentId(studentId))
                .thenReturn(true);  // <-- the hold

        // Act & Assert
        assertThatThrownBy(() -> registrationService.registerForClass(studentId, offeringId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student has an active hold and cannot register.");

        // The enrollment repository should never be touched when a hold blocks registration
        verify(enrollmentJDBCRepository, never()).save(any());
    }

    @Test
    void registerForClass_whenSeatsAvailable_enrollsStudent() {
        // Arrange
        long studentId = 2L, offeringId = 20L, courseId = 100L;

        when(studentJDBCRepository.findStudentById(studentId))
                .thenReturn(Optional.of(aStudent(studentId)));
        when(studentHoldJDBCRepository.existsActiveByStudentId(studentId))
                .thenReturn(false);
        when(courseOfferingJDBCRepository.findByOfferingId(offeringId))
                .thenReturn(Optional.of(anOffering(offeringId, courseId, 30)));
        when(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(any(Long.class), any(Long.class)))
                .thenReturn(false);
        when(enrollmentJDBCRepository.countActiveByOfferingId(any(Long.class)))
                .thenReturn(5L);    // 5 enrolled out of 30 seats — room available
        when(coursePrerequisiteJDBCRepository.findByCourseId(any(Long.class)))
                .thenReturn(List.of());  // no prerequisites
        when(courseJDBCRepository.findCourseById(any(Long.class)))
                .thenReturn(aCourse(courseId));
        // Make save() return whatever enrollment it receives (simulates DB assigning an id)
        when(enrollmentJDBCRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(999L);
            return e;
        });

        // Act
        Enrollment result = registrationService.registerForClass(studentId, offeringId);

        // Assert
        assertThat(result.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.ENROLLED);
        assertThat(result.getWaitlistPosition()).isNull();
        assertThat(result.getStudentId()).isEqualTo(studentId);
        assertThat(result.getOfferingId()).isEqualTo(offeringId);
    }

    @Test
    void registerForClass_whenCourseFull_waitlistsStudent() {
        // Arrange
        long studentId = 3L, offeringId = 30L, courseId = 200L;

        when(studentJDBCRepository.findStudentById(studentId))
                .thenReturn(Optional.of(aStudent(studentId)));
        when(studentHoldJDBCRepository.existsActiveByStudentId(studentId))
                .thenReturn(false);
        when(courseOfferingJDBCRepository.findByOfferingId(offeringId))
                .thenReturn(Optional.of(anOffering(offeringId, courseId, 1)));  // capacity = 1
        when(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(any(Long.class), any(Long.class)))
                .thenReturn(false);
        when(enrollmentJDBCRepository.countActiveByOfferingId(any(Long.class)))
                .thenReturn(1L);    // already 1 enrolled — course full
        when(coursePrerequisiteJDBCRepository.findByCourseId(any(Long.class)))
                .thenReturn(List.of());
        when(courseJDBCRepository.findCourseById(any(Long.class)))
                .thenReturn(aCourse(courseId));
        when(enrollmentJDBCRepository.countWaitlistedByOfferingId(any(Long.class)))
                .thenReturn(0L);    // no one else on waitlist yet — student gets position 1
        when(enrollmentJDBCRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(998L);
            return e;
        });

        // Act
        Enrollment result = registrationService.registerForClass(studentId, offeringId);

        // Assert
        assertThat(result.getEnrollmentStatus()).isEqualTo(Enrollment.EnrollmentStatus.WAITLISTED);
        assertThat(result.getWaitlistPosition()).isEqualTo(1);
    }

    @Test
    void registerForClass_whenAlreadyEnrolled_throwsException() {
        // Arrange
        long studentId = 4L, offeringId = 40L, courseId = 300L;

        when(studentJDBCRepository.findStudentById(studentId))
                .thenReturn(Optional.of(aStudent(studentId)));
        when(studentHoldJDBCRepository.existsActiveByStudentId(studentId))
                .thenReturn(false);
        when(courseOfferingJDBCRepository.findByOfferingId(offeringId))
                .thenReturn(Optional.of(anOffering(offeringId, courseId, 30)));
        when(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(studentId, offeringId))
                .thenReturn(true);  // <-- already enrolled

        // Act & Assert
        assertThatThrownBy(() -> registrationService.registerForClass(studentId, offeringId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already enrolled");

        verify(enrollmentJDBCRepository, never()).save(any());
    }
}
