package com.ewallet.user.service;

import com.ewallet.user.dto.request.LoginRequest;
import com.ewallet.user.dto.request.RefreshTokenRequest;
import com.ewallet.user.dto.request.RegisterRequest;
import com.ewallet.user.dto.request.SendOtpRequest;
import com.ewallet.user.dto.request.VerifyOtpRequest;
import com.ewallet.user.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse verifyOtp(VerifyOtpRequest request);

    void sendOtp(SendOtpRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout();
}
