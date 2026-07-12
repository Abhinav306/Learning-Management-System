package com.abhinav.lms.course.service;

import com.abhinav.lms.category.entity.Category;
import com.abhinav.lms.category.repository.CategoryRepository;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.course.dto.CourseRequest;
import com.abhinav.lms.course.dto.CourseResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.entity.DifficultyLevel;
import com.abhinav.lms.course.mapper.CourseMapper;
import com.abhinav.lms.course.repository.CourseRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponse createCourse(CourseRequest request, UserPrincipal currentUser) {
        log.info("Creating course by instructor: {}", currentUser.getUsername());

        User instructor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Course course = courseMapper.toEntity(request);
        course.setInstructor(instructor);
        course.setCategory(category);

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());
        return courseMapper.toResponse(savedCourse);
    }

    @Override
    @Cacheable(value = "course_details", key = "#id")
    public CourseResponse getCourseById(UUID id) {
        log.debug("Fetching course by id: {}", id);
        Course course = findCourseByIdOrThrow(id);
        return courseMapper.toResponse(course);
    }

    @Override
    public PagedResponse<CourseResponse> getAllCourses(int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching all courses");
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CourseResponse> coursePage = courseRepository.findAll(pageable)
                .map(courseMapper::toResponse);
        return PagedResponse.from(coursePage);
    }

    @Override
    public PagedResponse<CourseResponse> searchCourses(String keyword, UUID categoryId, DifficultyLevel difficulty,
                                                       CourseStatus status, UUID instructorId, int page, int size) {
        log.debug("Filtering courses dynamically");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Course> spec = Specification.where(null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("shortDescription")), "%" + keyword.toLowerCase() + "%")
            ));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (difficulty != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("difficulty"), difficulty));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (instructorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("instructor").get("id"), instructorId));
        }

        Page<CourseResponse> coursePage = courseRepository.findAll(spec, pageable)
                .map(courseMapper::toResponse);
        return PagedResponse.from(coursePage);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "courses", allEntries = true),
            @CacheEvict(value = "course_details", key = "#id")
    })
    public CourseResponse updateCourse(UUID id, CourseRequest request, UserPrincipal currentUser) {
        log.info("Updating course with ID: {}", id);
        Course course = findCourseByIdOrThrow(id);

        checkCourseOwnershipOrAdmin(course, currentUser);

        if (request.getCategoryId() != null && (course.getCategory() == null || !course.getCategory().getId().equals(request.getCategoryId()))) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            course.setCategory(category);
        }

        courseMapper.updateEntityFromRequest(request, course);
        Course updatedCourse = courseRepository.save(course);

        log.info("Course updated successfully with ID: {}", updatedCourse.getId());
        return courseMapper.toResponse(updatedCourse);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "courses", allEntries = true),
            @CacheEvict(value = "course_details", key = "#id")
    })
    public void deleteCourse(UUID id, UserPrincipal currentUser) {
        log.info("Deleting course with ID: {}", id);
        Course course = findCourseByIdOrThrow(id);

        checkCourseOwnershipOrAdmin(course, currentUser);

        courseRepository.delete(course);
        log.info("Course deleted successfully with ID: {}", id);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private Course findCourseByIdOrThrow(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    private void checkCourseOwnershipOrAdmin(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this course");
        }
    }
}
