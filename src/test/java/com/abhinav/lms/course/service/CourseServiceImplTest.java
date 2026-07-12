package com.abhinav.lms.course.service;

import com.abhinav.lms.category.entity.Category;
import com.abhinav.lms.category.repository.CategoryRepository;
import com.abhinav.lms.course.dto.CourseRequest;
import com.abhinav.lms.course.dto.CourseResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.mapper.CourseMapper;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
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
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    private User instructor;
    private UserPrincipal instructorPrincipal;
    private UserPrincipal anotherInstructorPrincipal;
    private Category category;
    private Course course;
    private CourseRequest courseRequest;
    private CourseResponse courseResponse;
    private UUID courseId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        instructor = User.builder()
                .id(UUID.randomUUID())
                .email("instructor@test.com")
                .role(UserRole.INSTRUCTOR)
                .build();

        instructorPrincipal = UserPrincipal.create(instructor);

        User anotherInstructor = User.builder()
                .id(UUID.randomUUID())
                .email("another@test.com")
                .role(UserRole.INSTRUCTOR)
                .build();
        anotherInstructorPrincipal = UserPrincipal.create(anotherInstructor);

        category = Category.builder()
                .id(categoryId)
                .name("Development")
                .build();

        course = Course.builder()
                .id(courseId)
                .title("Spring Boot")
                .instructor(instructor)
                .category(category)
                .build();

        courseRequest = CourseRequest.builder()
                .title("Spring Boot")
                .categoryId(categoryId)
                .build();

        courseResponse = CourseResponse.builder()
                .id(courseId)
                .title("Spring Boot")
                .build();
    }

    @Test
    void createCourse_Success() {
        when(userRepository.findById(instructorPrincipal.getId())).thenReturn(Optional.of(instructor));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(courseMapper.toEntity(any(CourseRequest.class))).thenReturn(course);
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseMapper.toResponse(any(Course.class))).thenReturn(courseResponse);

        CourseResponse result = courseService.createCourse(courseRequest, instructorPrincipal);

        assertNotNull(result);
        assertEquals("Spring Boot", result.getTitle());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void getCourseById_Success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(courseResponse);

        CourseResponse result = courseService.getCourseById(courseId);

        assertNotNull(result);
        assertEquals(courseId, result.getId());
    }

    @Test
    void getCourseById_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(UUID.randomUUID()));
    }

    @Test
    void updateCourse_Success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseMapper.toResponse(any(Course.class))).thenReturn(courseResponse);

        CourseResponse result = courseService.updateCourse(courseId, courseRequest, instructorPrincipal);

        assertNotNull(result);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void updateCourse_ThrowsBusinessException_WhenNotOwner() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertThrows(BusinessException.class, () -> courseService.updateCourse(courseId, courseRequest, anotherInstructorPrincipal));
    }

    @Test
    void deleteCourse_Success() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseService.deleteCourse(courseId, instructorPrincipal);

        verify(courseRepository, times(1)).delete(course);
    }
}
