package com.abhinav.lms.review.service;

import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.review.dto.ReviewRequest;
import com.abhinav.lms.review.dto.ReviewResponse;
import com.abhinav.lms.review.entity.Review;
import com.abhinav.lms.review.mapper.ReviewMapper;
import com.abhinav.lms.review.repository.ReviewRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User student;
    private UserPrincipal studentPrincipal;
    private Course course;
    private Enrollment enrollment;
    private Review review;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;
    private UUID courseId;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        student = User.builder()
                .id(UUID.randomUUID())
                .email("student@test.com")
                .role(UserRole.STUDENT)
                .build();
        studentPrincipal = UserPrincipal.create(student);

        course = Course.builder()
                .id(courseId)
                .title("Spring Boot")
                .build();

        enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        review = Review.builder()
                .id(reviewId)
                .course(course)
                .student(student)
                .rating(5)
                .comment("Excellent course!")
                .build();

        reviewRequest = ReviewRequest.builder()
                .rating(5)
                .comment("Excellent course!")
                .build();

        reviewResponse = ReviewResponse.builder()
                .id(reviewId)
                .rating(5)
                .comment("Excellent course!")
                .build();
    }

    @Test
    void createReview_Success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.of(enrollment));
        when(reviewRepository.existsByCourseIdAndStudentId(courseId, student.getId())).thenReturn(false);
        when(userRepository.findById(studentPrincipal.getId())).thenReturn(Optional.of(student));
        when(reviewMapper.toEntity(any(ReviewRequest.class))).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.createReview(courseId, reviewRequest, studentPrincipal);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_ThrowsBusinessException_WhenNotEnrolled() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> reviewService.createReview(courseId, reviewRequest, studentPrincipal));
    }

    @Test
    void createReview_ThrowsBusinessException_WhenAlreadyReviewed() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), courseId)).thenReturn(Optional.of(enrollment));
        when(reviewRepository.existsByCourseIdAndStudentId(courseId, student.getId())).thenReturn(true);

        assertThrows(BusinessException.class, () -> reviewService.createReview(courseId, reviewRequest, studentPrincipal));
    }
}
