package com.ewallet.user.service.impl;

import com.ewallet.common.exception.AppException;
import com.ewallet.common.exception.ErrorCode;
import com.ewallet.user.dto.request.LoginRequest;
import com.ewallet.user.dto.request.RefreshTokenRequest;
import com.ewallet.user.dto.request.RegisterRequest;
import com.ewallet.user.dto.response.AuthResponse;
import com.ewallet.user.dto.response.UserResponse;
import com.ewallet.user.entity.Role;
import com.ewallet.user.entity.User;
import com.ewallet.user.entity.UserStatus;
import com.ewallet.user.repository.RoleRepository;
import com.ewallet.user.repository.UserRepository;
import com.ewallet.user.security.JwtTokenProvider;
import com.ewallet.user.service.AuthService;
import com.ewallet.wallet.entity.Wallet;
import com.ewallet.wallet.entity.WalletStatus;
import com.ewallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone().trim())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null)
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        User savedUser = userRepository.save(user);

        // BR-01: Tạo ví chính mặc định (balance = 0, currency = VND)
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .status(WalletStatus.ACTIVE)
                .build();
        walletRepository.save(wallet);

        log.info("Registered user [{}] and created default wallet successfully", email);

        List<String> roles = List.of(userRole.getName());
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.from(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        log.info("User [{}] logged in successfully", email);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.from(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(UserResponse.from(user))
                .build();
    }

    @Override
    public void logout() {
        // Stateless JWT logout - Client discards tokens.
        // In Phase 6 with Redis, we can blacklist the access token until its TTL expires.
        log.info("User requested logout");
    }
}

