package com.abhinav.lms.enrollment.service;

import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.dto.EnrollmentResponse;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.mapper.EnrollmentMapper;
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
import java.util.Optional;
import java.util.UUID;

import com.abhinav.lms.notification.entity.NotificationType;
import com.abhinav.lms.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public EnrollmentResponse enrollCourse(UUID courseId, UserPrincipal currentUser) {
        log.info("Student ID: {} enrolling in course ID: {}", currentUser.getId(), courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("Instructors cannot enroll in their own courses");
        }

        User student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Optional<Enrollment> existingOpt = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId());
        if (existingOpt.isPresent()) {
            Enrollment existing = existingOpt.get();
            if (existing.getStatus() == EnrollmentStatus.ACTIVE) {
                throw new BusinessException("You are already actively enrolled in this course");
            }
            // Reactivate dropped/expired enrollment
            existing.setStatus(EnrollmentStatus.ACTIVE);
            existing.setEnrolledAt(LocalDateTime.now());
            existing.setCompletedAt(null);
            existing.setProgress(0.0);
            Enrollment reactivated = enrollmentRepository.save(existing);
            log.info("Enrollment reactivated for student: {} in course: {}", student.getEmail(), course.getTitle());

            // Notify instructor
            notificationService.sendNotification(
                    course.getInstructor(),
                    "New Student Enrolled",
                    "Student " + student.getFirstName() + " " + student.getLastName() + " has enrolled in course: " + course.getTitle(),
                    NotificationType.ENROLLMENT,
                    course.getId().toString(),
                    "COURSE"
            );

            return enrollmentMapper.toResponse(reactivated);
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .status(EnrollmentStatus.ACTIVE)
                .progress(0.0)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Student enrolled successfully with enrollment ID: {}", savedEnrollment.getId());

        // Notify instructor
        notificationService.sendNotification(
                course.getInstructor(),
                "New Student Enrolled",
                "Student " + student.getFirstName() + " " + student.getLastName() + " has enrolled in course: " + course.getTitle(),
                NotificationType.ENROLLMENT,
                course.getId().toString(),
                "COURSE"
        );

        return enrollmentMapper.toResponse(savedEnrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse dropCourse(UUID courseId, UserPrincipal currentUser) {
        log.info("Student ID: {} dropping course ID: {}", currentUser.getId(), courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "courseId", courseId));

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new BusinessException("You have already dropped this course");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment.setCompletedAt(null);
        enrollment.setProgress(0.0);

        Enrollment updated = enrollmentRepository.save(enrollment);
        log.info("Student successfully dropped course ID: {}", courseId);
        return enrollmentMapper.toResponse(updated);
    }

    @Override
    public PagedResponse<EnrollmentResponse> getStudentEnrollments(UserPrincipal currentUser, int page, int size) {
        log.debug("Fetching enrollments for student ID: {}", currentUser.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());

        Page<EnrollmentResponse> enrollmentPage = enrollmentRepository.findByStudentId(currentUser.getId(), pageable)
                .map(enrollmentMapper::toResponse);
        return PagedResponse.from(enrollmentPage);
    }

    @Override
    public PagedResponse<EnrollmentResponse> getCourseEnrollments(UUID courseId, UserPrincipal currentUser, int page, int size) {
        log.debug("Fetching roster for course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to view the enrollment roster for this course");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        Page<EnrollmentResponse> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable)
                .map(enrollmentMapper::toResponse);
        return PagedResponse.from(enrollmentPage);
    }
}
