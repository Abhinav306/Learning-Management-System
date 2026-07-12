package com.abhinav.lms.user.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.user.dto.CreateUserRequest;
import com.abhinav.lms.user.dto.UpdateUserRequest;
import com.abhinav.lms.user.dto.UserResponse;
import com.abhinav.lms.user.entity.UserRole;
import com.abhinav.lms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations for user accounts")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user")
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ApiResponse.created(user, "User created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getUserById(
            @PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get user by email")
    public ApiResponse<UserResponse> getUserByEmail(
            @PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ApiResponse.success(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (paginated)")
    public ApiResponse<PagedResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        PagedResponse<UserResponse> users = userService.getAllUsers(page, size, sortBy, sortDir);
        return ApiResponse.success(users);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (paginated)")
    public ApiResponse<PagedResponse<UserResponse>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<UserResponse> users = userService.getUsersByRole(role, page, size);
        return ApiResponse.success(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users by name or email")
    public ApiResponse<PagedResponse<UserResponse>> searchUsers(
            @Parameter(description = "Search keyword")
            @RequestParam String keyword,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<UserResponse> users = userService.searchUsers(keyword, page, size);
        return ApiResponse.success(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @Operation(summary = "Update user")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ApiResponse.success(user, "User updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user")
    public void deleteUser(
            @PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
