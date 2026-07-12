package com.abhinav.lms.user.service;

import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.exception.DuplicateResourceException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.user.dto.CreateUserRequest;
import com.abhinav.lms.user.dto.UpdateUserRequest;
import com.abhinav.lms.user.dto.UserResponse;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.abhinav.lms.user.mapper.UserMapper;
import com.abhinav.lms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private CreateUserRequest createRequest;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("student@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.STUDENT)
                .enabled(true)
                .build();

        createRequest = CreateUserRequest.builder()
                .email("student@test.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.STUDENT)
                .build();

        userResponse = UserResponse.builder()
                .id(userId)
                .email("student@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(CreateUserRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.createUser(createRequest);

        assertNotNull(result);
        assertEquals("student@test.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ThrowsDuplicateResourceException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(createRequest));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void getUserById_ThrowsResourceNotFoundException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(UUID.randomUUID()));
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserByEmail("student@test.com");

        assertNotNull(result);
        assertEquals("student@test.com", result.getEmail());
    }

    @Test
    void searchUsers_Success() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.search(anyString(), any(Pageable.class))).thenReturn(page);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PagedResponse<UserResponse> result = userService.searchUsers("John", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }
}
