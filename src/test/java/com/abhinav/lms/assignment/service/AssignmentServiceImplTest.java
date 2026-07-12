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
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.notification.service.NotificationService;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.abhinav.lms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentSubmissionRepository submissionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssignmentMapper assignmentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private User student;
    private UserPrincipal studentPrincipal;
    private User instructor;
    private UserPrincipal instructorPrincipal;
    private Course course;
    private Assignment assignment;
    private AssignmentSubmission submission;
    private AssignmentRequest assignmentRequest;
    private AssignmentResponse assignmentResponse;
    private SubmissionRequest submissionRequest;
    private SubmissionResponse submissionResponse;
    private UUID courseId;
    private UUID assignmentId;
    private UUID submissionId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
        submissionId = UUID.randomUUID();

        student = User.builder()
                .id(UUID.randomUUID())
                .email("student@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.STUDENT)
                .build();
        studentPrincipal = UserPrincipal.create(student);

        instructor = User.builder()
                .id(UUID.randomUUID())
                .email("instructor@test.com")
                .role(UserRole.INSTRUCTOR)
                .build();
        instructorPrincipal = UserPrincipal.create(instructor);

        course = Course.builder()
                .id(courseId)
                .title("Spring Boot")
                .instructor(instructor)
                .build();

        assignment = Assignment.builder()
                .id(assignmentId)
                .title("Midterm Assignment")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(5))
                .course(course)
                .build();

        submission = AssignmentSubmission.builder()
                .id(submissionId)
                .assignment(assignment)
                .student(student)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        assignmentRequest = AssignmentRequest.builder()
                .title("Midterm Assignment")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();

        assignmentResponse = AssignmentResponse.builder()
                .id(assignmentId)
                .title("Midterm Assignment")
                .build();

        submissionRequest = SubmissionRequest.builder()
                .content("My assignment solutions")
                .build();

        submissionResponse = SubmissionResponse.builder()
                .id(submissionId)
                .status(SubmissionStatus.SUBMITTED)
                .build();
    }

    @Test
    void createAssignment_Success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(assignmentMapper.toEntity(any(AssignmentRequest.class))).thenReturn(assignment);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assignmentMapper.toResponse(any(Assignment.class))).thenReturn(assignmentResponse);

        AssignmentResponse result = assignmentService.createAssignment(courseId, assignmentRequest, instructorPrincipal);

        assertNotNull(result);
        assertEquals("Midterm Assignment", result.getTitle());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void submitAssignment_Success() {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(studentPrincipal.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.of(enrollment));
        when(submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, student.getId())).thenReturn(false);
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(submission);
        when(assignmentMapper.toResponse(any(AssignmentSubmission.class))).thenReturn(submissionResponse);

        SubmissionResponse result = assignmentService.submitAssignment(assignmentId, submissionRequest, studentPrincipal);

        assertNotNull(result);
        assertEquals(SubmissionStatus.SUBMITTED, result.getStatus());
        verify(submissionRepository, times(1)).save(any(AssignmentSubmission.class));
    }

    @Test
    void submitAssignment_ThrowsBusinessException_WhenNotEnrolled() {
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> assignmentService.submitAssignment(assignmentId, submissionRequest, studentPrincipal));
    }

    @Test
    void gradeSubmission_Success() {
        GradeRequest gradeRequest = GradeRequest.builder()
                .grade(95)
                .feedback("Excellent work!")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(userRepository.findById(instructorPrincipal.getId())).thenReturn(Optional.of(instructor));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(submission);
        when(assignmentMapper.toResponse(any(AssignmentSubmission.class))).thenReturn(submissionResponse);

        SubmissionResponse result = assignmentService.gradeSubmission(assignmentId, submissionId, gradeRequest, instructorPrincipal);

        assertNotNull(result);
        verify(submissionRepository, times(1)).save(submission);
        assertEquals(95, submission.getGrade());
        assertEquals(SubmissionStatus.GRADED, submission.getStatus());
    }
}
