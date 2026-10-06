package com.ewallet.user.controller;

import com.ewallet.common.dto.response.ApiResponse;
import com.ewallet.user.dto.request.UpdateProfileRequest;
import com.ewallet.user.dto.response.UserResponse;
import com.ewallet.user.security.SecurityUtils;
import com.ewallet.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UserResponse response = userService.getProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        UserResponse response = userService.updateProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", response));
    }
}
