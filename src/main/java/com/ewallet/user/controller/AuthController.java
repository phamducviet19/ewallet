package com.ewallet.user.controller;

import com.ewallet.common.dto.response.ApiResponse;
import com.ewallet.user.dto.request.LoginRequest;
import com.ewallet.user.dto.request.RefreshTokenRequest;
import com.ewallet.user.dto.request.RegisterRequest;
import com.ewallet.user.dto.request.SendOtpRequest;
import com.ewallet.user.dto.request.VerifyOtpRequest;
import com.ewallet.user.dto.response.AuthResponse;
import com.ewallet.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Đăng ký tài khoản thành công. Mã OTP đã được gửi, vui lòng xác thực để kích hoạt ví.",
                        response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        String message = Boolean.TRUE.equals(response.getOtpRequired())
                ? "Thông tin hợp lệ. Vui lòng nhập mã OTP để hoàn tất đăng nhập."
                : "Đăng nhập thành công";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Xác thực mã OTP thành công", response));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Mã OTP mới đã được gửi thành công", null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }
}
