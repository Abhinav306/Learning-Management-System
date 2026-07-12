package com.abhinav.lms.assignment.service;

import com.abhinav.lms.assignment.dto.AssignmentRequest;
import com.abhinav.lms.assignment.dto.AssignmentResponse;
import com.abhinav.lms.assignment.dto.GradeRequest;
import com.abhinav.lms.assignment.dto.SubmissionRequest;
import com.abhinav.lms.assignment.dto.SubmissionResponse;
import com.abhinav.lms.assignment.entity.Assignment;
import com.abhinav.lms.assignment.entity.AssignmentSubmission;
import com.abhinav.lms.assignment.entity.SubmissionStatus;
import com.abhinav.lms.assignment.mapper.AssignmentMapper;
import com.abhinav.lms.assignment.repository.AssignmentRepository;
import com.abhinav.lms.assignment.repository.AssignmentSubmissionRepository;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.course.repository.LessonRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.abhinav.lms.notification.entity.NotificationType;
import com.abhinav.lms.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AssignmentMapper assignmentMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(UUID courseId, AssignmentRequest request, UserPrincipal currentUser) {
        log.info("Creating assignment in course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        checkCourseOwnershipOrAdmin(course, currentUser);

        Lesson lesson = null;
        if (request.getLessonId() != null) {
            lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", request.getLessonId()));
            if (!lesson.getSection().getCourse().getId().equals(courseId)) {
                throw new BusinessException("Lesson does not belong to the specified course");
            }
        }

        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setCourse(course);
        assignment.setLesson(lesson);

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Assignment created successfully with ID: {}", saved.getId());
        return assignmentMapper.toResponse(saved);
    }

    @Override
    public AssignmentResponse getAssignmentById(UUID assignmentId) {
        log.debug("Fetching assignment ID: {}", assignmentId);
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByCourse(UUID courseId) {
        log.debug("Fetching all assignments in course ID: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        return assignmentMapper.toResponseList(assignments);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(UUID assignmentId, AssignmentRequest request, UserPrincipal currentUser) {
        log.info("Updating assignment ID: {}", assignmentId);
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        checkCourseOwnershipOrAdmin(assignment.getCourse(), currentUser);

        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", request.getLessonId()));
            if (!lesson.getSection().getCourse().getId().equals(assignment.getCourse().getId())) {
                throw new BusinessException("Lesson does not belong to the specified course");
            }
            assignment.setLesson(lesson);
        } else {
            assignment.setLesson(null);
        }

        assignmentMapper.updateEntityFromRequest(request, assignment);
        Assignment updated = assignmentRepository.save(assignment);
        log.info("Assignment updated successfully with ID: {}", assignmentId);
        return assignmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAssignment(UUID assignmentId, UserPrincipal currentUser) {
        log.info("Deleting assignment ID: {}", assignmentId);
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        checkCourseOwnershipOrAdmin(assignment.getCourse(), currentUser);

        assignmentRepository.delete(assignment);
        log.info("Assignment deleted successfully with ID: {}", assignmentId);
    }

    @Override
    @Transactional
    public SubmissionResponse submitAssignment(UUID assignmentId, SubmissionRequest request, UserPrincipal currentUser) {
        log.info("Student ID: {} submitting assignment ID: {}", currentUser.getId(), assignmentId);
        Assignment assignment = findAssignmentOrThrow(assignmentId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), assignment.getCourse().getId())
                .orElseThrow(() -> new BusinessException("Only enrolled students can submit assignments"));

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED || enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessException("Cannot submit assignments for dropped or expired course enrollments");
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, currentUser.getId())) {
            throw new BusinessException("You have already submitted this assignment");
        }

        User student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            status = SubmissionStatus.LATE;
        }

        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .student(student)
                .content(request.getContent())
                .fileUrl(request.getFileUrl())
                .submittedAt(LocalDateTime.now())
                .status(status)
                .build();

        AssignmentSubmission saved = submissionRepository.save(submission);
        log.info("Assignment submission created successfully with ID: {}", saved.getId());
        return assignmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SubmissionResponse gradeSubmission(UUID assignmentId, UUID submissionId, GradeRequest request, UserPrincipal currentUser) {
        log.info("Instructor ID: {} grading submission ID: {}", currentUser.getId(), submissionId);
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        checkCourseOwnershipOrAdmin(assignment.getCourse(), currentUser);

        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        if (!submission.getAssignment().getId().equals(assignmentId)) {
            throw new BusinessException("Submission does not belong to the specified assignment");
        }

        if (request.getGrade() < 0 || request.getGrade() > assignment.getMaxScore()) {
            throw new BusinessException("Grade must be between 0 and " + assignment.getMaxScore());
        }

        User instructor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        submission.setGrade(request.getGrade());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(instructor);

        AssignmentSubmission updated = submissionRepository.save(submission);
        log.info("Submission graded successfully with ID: {}", submissionId);

        // Notify student
        notificationService.sendNotification(
                submission.getStudent(),
                "Assignment Graded",
                "Your submission for " + assignment.getTitle() + " has been graded: " + request.getGrade() + "/" + assignment.getMaxScore(),
                NotificationType.GRADE,
                assignment.getId().toString(),
                "ASSIGNMENT"
        );

        return assignmentMapper.toResponse(updated);
    }

    @Override
    public PagedResponse<SubmissionResponse> getAssignmentSubmissions(UUID assignmentId, int page, int size, UserPrincipal currentUser) {
        log.debug("Fetching submissions roster for assignment ID: {}", assignmentId);
        Assignment assignment = findAssignmentOrThrow(assignmentId);
        checkCourseOwnershipOrAdmin(assignment.getCourse(), currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<SubmissionResponse> submissionsPage = submissionRepository.findByAssignmentId(assignmentId, pageable)
                .map(assignmentMapper::toResponse);
        return PagedResponse.from(submissionsPage);
    }

    @Override
    public SubmissionResponse getMySubmission(UUID assignmentId, UserPrincipal currentUser) {
        log.debug("Student ID: {} fetching submission for assignment ID: {}", currentUser.getId(), assignmentId);
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment", "id", assignmentId);
        }
        AssignmentSubmission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "assignmentId/studentId", assignmentId + "/" + currentUser.getId()));
        return assignmentMapper.toResponse(submission);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private Assignment findAssignmentOrThrow(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));
    }

    private void checkCourseOwnershipOrAdmin(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this course assignment content");
        }
    }
}
