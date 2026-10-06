package com.ewallet.admin.controller;

import com.ewallet.common.dto.response.ApiResponse;
import com.ewallet.common.dto.response.PageResponse;
import com.ewallet.user.dto.request.UpdateUserStatusRequest;
import com.ewallet.user.dto.response.UserResponse;
import com.ewallet.user.entity.UserStatus;
import com.ewallet.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<UserResponse> response = userService.getAllUsers(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        UserResponse response = userService.updateUserStatus(id, request.getStatus());
        String msg = request.getStatus() == UserStatus.LOCKED ? "Khóa tài khoản thành công" : "Mở khóa tài khoản thành công";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}
