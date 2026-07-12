package com.abhinav.lms.user.service;

import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.user.dto.CreateUserRequest;
import com.abhinav.lms.user.dto.UpdateUserRequest;
import com.abhinav.lms.user.dto.UserResponse;
import com.abhinav.lms.user.entity.UserRole;

import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    UserResponse getUserByEmail(String email);

    PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir);

    PagedResponse<UserResponse> getUsersByRole(UserRole role, int page, int size);

    PagedResponse<UserResponse> searchUsers(String keyword, int page, int size);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);
}
