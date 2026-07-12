package com.abhinav.lms.review.service;

import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.review.dto.CourseReviewsSummaryResponse;
import com.abhinav.lms.review.dto.ReviewRequest;
import com.abhinav.lms.review.dto.ReviewResponse;
import com.abhinav.lms.review.entity.Review;
import com.abhinav.lms.review.mapper.ReviewMapper;
import com.abhinav.lms.review.repository.ReviewRepository;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID courseId, ReviewRequest request, UserPrincipal currentUser) {
        log.info("Student ID: {} creating review for course ID: {}", currentUser.getId(), courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new BusinessException("Only enrolled students can review courses"));

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED || enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessException("Cannot review courses for inactive or dropped course enrollments");
        }

        if (reviewRepository.existsByCourseIdAndStudentId(courseId, currentUser.getId())) {
            throw new BusinessException("You have already reviewed this course");
        }

        User student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Review review = reviewMapper.toEntity(request);
        review.setCourse(course);
        review.setStudent(student);

        Review saved = reviewRepository.save(review);
        log.info("Review created successfully with ID: {}", saved.getId());
        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID courseId, UUID reviewId, ReviewRequest request, UserPrincipal currentUser) {
        log.info("Student ID: {} updating review ID: {}", currentUser.getId(), reviewId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Review does not belong to the specified course");
        }

        if (!review.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this review");
        }

        reviewMapper.updateEntityFromRequest(request, review);
        Review updated = reviewRepository.save(review);
        log.info("Review updated successfully with ID: {}", reviewId);
        return reviewMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReview(UUID courseId, UUID reviewId, UserPrincipal currentUser) {
        log.info("Deleting review ID: {}", reviewId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Review does not belong to the specified course");
        }

        if (!currentUser.getRole().name().equals("ADMIN") &&
                !review.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to delete this review");
        }

        reviewRepository.delete(review);
        log.info("Review deleted successfully with ID: {}", reviewId);
    }

    @Override
    public CourseReviewsSummaryResponse getCourseReviews(UUID courseId, int page, int size) {
        log.debug("Fetching reviews summary for course ID: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> reviewsPage = reviewRepository.findByCourseId(courseId, pageable)
                .map(reviewMapper::toResponse);

        Double averageRating = reviewRepository.findAverageRatingByCourseId(courseId);
        double roundedAverage = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;

        return CourseReviewsSummaryResponse.builder()
                .averageRating(roundedAverage)
                .totalReviews(reviewsPage.getTotalElements())
                .reviews(PagedResponse.from(reviewsPage))
                .build();
    }
}
