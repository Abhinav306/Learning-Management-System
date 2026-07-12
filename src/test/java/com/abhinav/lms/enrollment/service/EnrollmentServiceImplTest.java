package com.abhinav.lms.enrollment.service;

import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.dto.EnrollmentResponse;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.mapper.EnrollmentMapper;
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
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private User student;
    private UserPrincipal studentPrincipal;
    private User instructor;
    private UserPrincipal instructorPrincipal;
    private Course course;
    private Enrollment enrollment;
    private EnrollmentResponse enrollmentResponse;
    private UUID courseId;
    private UUID enrollmentId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        enrollmentId = UUID.randomUUID();

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

        enrollment = Enrollment.builder()
                .id(enrollmentId)
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .progress(0.0)
                .build();

        enrollmentResponse = EnrollmentResponse.builder()
                .id(enrollmentId)
                .status(EnrollmentStatus.ACTIVE)
                .progress(0.0)
                .build();
    }

    @Test
    void enrollCourse_Success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentPrincipal.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(enrollmentResponse);

        EnrollmentResponse result = enrollmentService.enrollCourse(courseId, studentPrincipal);

        assertNotNull(result);
        assertEquals(EnrollmentStatus.ACTIVE, result.getStatus());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enrollCourse_ThrowsBusinessException_WhenInstructorEnrollsInOwnCourse() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertThrows(BusinessException.class, () -> enrollmentService.enrollCourse(courseId, instructorPrincipal));
    }

    @Test
    void enrollCourse_ThrowsBusinessException_WhenAlreadyEnrolled() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentPrincipal.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId())).thenReturn(Optional.of(enrollment));

        assertThrows(BusinessException.class, () -> enrollmentService.enrollCourse(courseId, studentPrincipal));
    }

    @Test
    void dropCourse_Success() {
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(enrollmentMapper.toResponse(any(Enrollment.class))).thenReturn(enrollmentResponse);

        EnrollmentResponse result = enrollmentService.dropCourse(courseId, studentPrincipal);

        assertNotNull(result);
        verify(enrollmentRepository, times(1)).save(enrollment);
        assertEquals(EnrollmentStatus.DROPPED, enrollment.getStatus());
    }

    @Test
    void dropCourse_ThrowsBusinessException_WhenAlreadyDropped() {
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.of(enrollment));

        assertThrows(BusinessException.class, () -> enrollmentService.dropCourse(courseId, studentPrincipal));
    }
}
