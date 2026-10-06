package com.ewallet.user.service;

import com.ewallet.common.dto.response.PageResponse;
import com.ewallet.user.dto.request.UpdateProfileRequest;
import com.ewallet.user.dto.response.UserResponse;
import com.ewallet.user.entity.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse getProfile(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);

    PageResponse<UserResponse> getAllUsers(String search, UserStatus status, Pageable pageable);

    UserResponse updateUserStatus(UUID userId, UserStatus status);
}
