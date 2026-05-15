package com.university.UniversityPortal.feature.registration.service;

import com.university.UniversityPortal.feature.registration.dto.BatchRegistrationResult;
import com.university.UniversityPortal.feature.course.entity.Course;
import com.university.UniversityPortal.feature.course.entity.CoursePrerequisites;
import com.university.UniversityPortal.feature.courseoffering.entity.CourseOffering;
import com.university.UniversityPortal.feature.enrollment.entity.Enrollment;
import com.university.UniversityPortal.feature.wishlist.entity.Wishlist;
import com.university.UniversityPortal.feature.course.repository.CourseJDBCRepository;
import com.university.UniversityPortal.feature.courseoffering.repository.CourseOfferingJDBCRepository;
import com.university.UniversityPortal.feature.course.repository.CoursePrerequisiteJDBCRepository;
import com.university.UniversityPortal.feature.wishlist.repository.WishlistJDBCRepository;
import com.university.UniversityPortal.feature.enrollment.repository.EnrollmentJDBCRepository;
import com.university.UniversityPortal.feature.studenthold.repository.StudentHoldJDBCRepository;
import com.university.UniversityPortal.feature.student.repository.StudentJDBCRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: add @transactional where needed
//TODO: replace repository call with DAO methods that use SQL
//      Explicitly load related data instead of relying on JPA relationships where appropriate for performance
//      Ensure all write operations are performed with transactions using JDBC
//      Update tests to reflect the new data access pattern and SQL-based data setup
@Service
public class RegistrationService {

    private final EnrollmentJDBCRepository enrollmentJDBCRepository;
    private final StudentJDBCRepository studentJDBCRepository;
    private final CourseOfferingJDBCRepository courseOfferingJDBCRepository;
    private final StudentHoldJDBCRepository studentHoldJDBCRepository;
    private final WishlistJDBCRepository wishlistJDBCRepository;
    private final CourseJDBCRepository courseJDBCRepository;
    private final CoursePrerequisiteJDBCRepository coursePrerequisiteJDBCRepository;

    //constructor
    public RegistrationService(EnrollmentJDBCRepository enrollmentJDBCRepository,
                               StudentJDBCRepository studentJDBCRepository,
                               CourseOfferingJDBCRepository courseOfferingJDBCRepository, StudentHoldJDBCRepository studentHoldJDBCRepository, WishlistJDBCRepository wishlistJDBCRepository, CourseJDBCRepository courseJDBCRepository, CoursePrerequisiteJDBCRepository coursePrerequisiteJDBCRepository) {
        this.enrollmentJDBCRepository = enrollmentJDBCRepository;
        this.studentJDBCRepository = studentJDBCRepository;
        this.courseOfferingJDBCRepository = courseOfferingJDBCRepository;
        this.studentHoldJDBCRepository = studentHoldJDBCRepository;
        this.wishlistJDBCRepository = wishlistJDBCRepository;
        this.courseJDBCRepository = courseJDBCRepository;
        this.coursePrerequisiteJDBCRepository = coursePrerequisiteJDBCRepository;
    }

    //TODO perform integration test
    @Transactional
    public Wishlist addToWishlist(Long studentId, long offeringId) {

        //TODO may not be needed
        //1. load student
        studentJDBCRepository.findStudentById(studentId).orElseThrow(() -> new RuntimeException("student not found: " + studentId));

        //2. load offering
        courseOfferingJDBCRepository.findByOfferingId(offeringId)
                .orElseThrow(() -> new RuntimeException("offering not found: " + offeringId));

        //3. check if already in wishlist
        //TODO change this to be JDBC instead of JPA
        if(wishlistJDBCRepository.existsByStudentIdAndOfferingId(studentId, offeringId)) {
            throw new RuntimeException("student " + studentId + " already has offering " + offeringId + " in wishlist");
        }

        //4. create wishlist record
        Wishlist item = new Wishlist();
        item.setStudentId(studentId);
        item.setOfferingId(offeringId);
        item.setAddedAt(LocalDateTime.now());

        return wishlistJDBCRepository.save(item);
    }

    //TODO perform integration test
    @Transactional
    public void removeFromWishlist(Long studentId, long offeringId) {
        //find wishlist record
        int rows = wishlistJDBCRepository.deleteByStudentIdAndOfferingId(studentId, offeringId);

        if (rows == 0){
            throw new RuntimeException("wishlist item not found for student " + studentId + " and offering " + offeringId);
        }
    }

    //TODO perform integration test
    @Transactional
    public List<BatchRegistrationResult> registerAllFromWishlist(Long studentId, String term) {

        List<Wishlist> wishlistItems = wishlistJDBCRepository.findByStudentIdAndTerm(studentId, term);
        List<BatchRegistrationResult> results = new ArrayList<>();

        for (Wishlist item : wishlistItems) {
            Long offeringId = item.getOfferingId();
            try {
                Enrollment enrollment = registerForClass(studentId, offeringId);
                results.add(new BatchRegistrationResult(offeringId, true, "Registered successfully as " + enrollment.getEnrollmentStatus()));
            } catch (RuntimeException e) {
                results.add(new BatchRegistrationResult(offeringId, false, e.getMessage()));
            }
        }

        return results;
    }
    //TODO: refactor all instances of enrollment
    //service to register for classes
    //TODO: add checks for unique student IDs, valid course capacity, and prerequisites
    @Transactional
    public Enrollment registerForClass (Long studentId, Long offeringId) {

        //1. load student
        studentJDBCRepository.findStudentById(studentId).orElseThrow(() -> new RuntimeException("student not found: " + studentId));

        //TODO provide more detailed hold information
        //2. check for holds
        if(studentHoldJDBCRepository.existsActiveByStudentId(studentId)){
            throw new RuntimeException("Student has an active hold and cannot register.");
        }

        //3. lock offering row to prevent seat race condition
        CourseOffering offering = courseOfferingJDBCRepository.findByOfferingId(offeringId)
                .orElseThrow(() -> new RuntimeException("offering not found: " + offeringId));

        //4. check if already enrolled
        if(enrollmentJDBCRepository.existsByStudentIdAndOfferingId(studentId, offeringId)) {
            throw new RuntimeException("student " + studentId + " is already enrolled in offering " + offeringId);
        }

        //5. check seat capacity
        long enrolledCount = enrollmentJDBCRepository.countActiveByOfferingId(offeringId);
        boolean hasSeats = enrolledCount < offering.getSeatCapacity();

        //TODO prerequisites looks sus
        //6. Prerequisites check
        Long courseId = offering.getCourseId();
        List<CoursePrerequisites> prerequisites = coursePrerequisiteJDBCRepository.findByCourseId(courseId);

        //load course info for error message
        Course course = courseJDBCRepository.findCourseById(courseId);

        //TODO: later, implement logic to check student's grade for course
        if(!prerequisites.isEmpty()) {
            Map<Integer, List<CoursePrerequisites>> grouped = prerequisites.stream().collect(Collectors.groupingBy(CoursePrerequisites::getGroupId));
            for (List<CoursePrerequisites> group : grouped.values()) {
                boolean satisfied = group.stream()
                        .anyMatch(prereq -> hasSatisfiedPrerequisite(studentId, prereq));

                if (!satisfied) {
                    throw new RuntimeException("You have not completed all prerequisites for " + course.getCourseCode());
                    //TODO, show missing prerequisites in the error message
                }
            }
        }

        //6. create enrollment record
        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setOfferingId(offeringId);
        e.setEnrolledAt(LocalDateTime.now());
        e.setLastUpdated(LocalDateTime.now());
        e.setCreditsAttempted(0);   //TODO: set to offering/ course credit hours

        if (hasSeats) {
            e.setEnrollmentStatus(Enrollment.EnrollmentStatus.ENROLLED);
            e.setWaitlistPosition(null);
            System.out.println("You have successfully enrolled in offering for " + course.getCourseCode());
        } else {
            long waitlistedCount = enrollmentJDBCRepository.countWaitlistedByOfferingId(offeringId);
            int nextPosition = (int) (waitlistedCount + 1);
            e.setEnrollmentStatus(Enrollment.EnrollmentStatus.WAITLISTED);
            //set waitlist position
            e.setWaitlistPosition(nextPosition);
            System.out.println("You have successfully waitlisted in offering for " + course.getCourseCode());
        }

        return enrollmentJDBCRepository.save(e);
    }

    private boolean hasSatisfiedPrerequisite(Long studentId, CoursePrerequisites prerequisite) {
        CoursePrerequisites.PrerequisteType type = prerequisite.getPrerequisiteType();
        if (type == CoursePrerequisites.PrerequisteType.COURSE) {
            return enrollmentJDBCRepository.hasCompletedCourseWithMinGrade(
                    studentId,
                    prerequisite.getRequiredCourseId(),
                    null);
        }

        if (type == CoursePrerequisites.PrerequisteType.GRADE) {
            return enrollmentJDBCRepository.hasCompletedCourseWithMinGrade(
                    studentId,
                    prerequisite.getRequiredCourseId(),
                    prerequisite.getMinGradeValue());
        }
        return false;
    }


    //TODO: Map invalid dropped class to 400 or 409 error with an exception handler
    @Transactional
    public Enrollment dropClass(Long studentId, Long offeringId) {

        //1. find enrollment or waitlist record
        Enrollment enrollment = enrollmentJDBCRepository.findByStudentIdAndOfferingIdWithStatuses(
                studentId,
                offeringId,
                List.of(Enrollment.EnrollmentStatus.ENROLLED, Enrollment.EnrollmentStatus.WAITLISTED)
        ).orElseThrow(() -> new RuntimeException("Student is not enrolled or waitlisted for this offering."));

        //store old status
        //TODO: refactor
        Enrollment.EnrollmentStatus oldStatus = enrollment.getEnrollmentStatus();

        //capture dropped position
        //TODO: refactor
        Integer droppedPosition = enrollment.getWaitlistPosition();
        LocalDateTime now = LocalDateTime.now();

        //2. update enrollment to dropped
        enrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollment.setDroppedAt(now);
        enrollment.setLastUpdated(now);
        enrollment.setWaitlistPosition(null);
        enrollmentJDBCRepository.save(enrollment);

        //3. Only open up a spot if the student was enrolled, not waitlisted
        //promote the first waitlisted student (position 1) to ENROLLED
        //shift everyone behind them up by 1 in the waitlist
        if(oldStatus == Enrollment.EnrollmentStatus.ENROLLED) {
            //promote next waitlisted student, if any
            enrollmentJDBCRepository.findFirstWaitlistedByOffering(offeringId)
                    .ifPresent(nextInLine -> {

                        Integer promotedFromPos = nextInLine.getWaitlistPosition();
                        LocalDateTime nowInner = LocalDateTime.now();

                        nextInLine.setEnrollmentStatus(Enrollment.EnrollmentStatus.ENROLLED);
                        nextInLine.setWaitlistPosition(null);
                        nextInLine.setLastUpdated(nowInner);
                        enrollmentJDBCRepository.update(nextInLine);

                        if(promotedFromPos != null) {
                            List<Enrollment> remaining = enrollmentJDBCRepository.findWaitlistedWithPositionGreaterThan(offeringId, promotedFromPos);

                            for (Enrollment en : remaining) {
                                en.setWaitlistPosition(en.getWaitlistPosition() - 1);
                                en.setLastUpdated(nowInner);
                            }
                            enrollmentJDBCRepository.updateAll(remaining);
                        }
                    });
            return enrollment;
        }


        //if the student was waitlisted, promote the next in line by adjusting their waitlist position
        if(oldStatus == Enrollment.EnrollmentStatus.WAITLISTED && droppedPosition != null) {
            List<Enrollment> shifted = enrollmentJDBCRepository.findWaitlistedWithPositionGreaterThan(offeringId, droppedPosition);

            for (Enrollment en : shifted) {
                en.setWaitlistPosition(en.getWaitlistPosition() - 1);
                en.setLastUpdated(LocalDateTime.now());
            }
            enrollmentJDBCRepository.updateAll(shifted);
        }
        return enrollment;
    }

    //TODO add wishlist helper method?
    //  public List<Wishlist> getWishlistsForStudent(Long studentId) {
    //    return wishlistJdbcRepository.findByStudentId(studentId);
    //}
}
