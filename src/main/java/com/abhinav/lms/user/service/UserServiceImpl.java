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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        User user = findUserByIdOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching all users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> userPage = userRepository.findAll(pageable)
                .map(userMapper::toResponse);

        return PagedResponse.from(userPage);
    }

    @Override
    public PagedResponse<UserResponse> getUsersByRole(UserRole role, int page, int size) {
        log.debug("Fetching users by role: {}", role);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserResponse> userPage = userRepository.findByRole(role, pageable)
                .map(userMapper::toResponse);

        return PagedResponse.from(userPage);
    }

    @Override
    public PagedResponse<UserResponse> searchUsers(String keyword, int page, int size) {
        log.debug("Searching users with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserResponse> userPage = userRepository.search(keyword, pageable)
                .map(userMapper::toResponse);

        return PagedResponse.from(userPage);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = findUserByIdOrThrow(id);

        // Check email uniqueness if email is being changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
        }

        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with id: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);

        User user = findUserByIdOrThrow(id);
        userRepository.delete(user);

        log.info("User deleted successfully with id: {}", id);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private User findUserByIdOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
