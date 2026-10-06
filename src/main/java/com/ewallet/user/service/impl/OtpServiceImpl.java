package com.ewallet.user.service.impl;

import com.ewallet.common.exception.AppException;
import com.ewallet.common.exception.ErrorCode;
import com.ewallet.user.dto.OtpData;
import com.ewallet.user.entity.OtpPurpose;
import com.ewallet.user.entity.User;
import com.ewallet.user.entity.UserStatus;
import com.ewallet.user.repository.UserRepository;
import com.ewallet.user.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final String REDIS_OTP_PREFIX = "otp:%s:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateAndSendOtp(User user, OtpPurpose purpose) {
        String email = user.getEmail().toLowerCase().trim();
        String rawOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String hashedOtp = passwordEncoder.encode(rawOtp);

        OtpData otpData = OtpData.builder()
                .otpHash(hashedOtp)
                .attempts(0)
                .build();

        String redisKey = buildKey(purpose, email);
        redisTemplate.opsForValue().set(redisKey, otpData, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        log.info("""

                ==================================================
                [MOCK OTP NOTIFICATION - REDIS]
                Email   : {}
                Purpose : {}
                OTP Code: {} (Hiệu lực: {} phút trên Redis)
                ==================================================
                """, email, purpose, rawOtp, OTP_EXPIRY_MINUTES);

        return rawOtp;
    }

    @Override
    public User verifyOtp(String email, String rawOtp, OtpPurpose purpose) {
        String normalizedEmail = email.toLowerCase().trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.USER_LOCKED);
        }

        String redisKey = buildKey(purpose, normalizedEmail);
        Object cachedValue = redisTemplate.opsForValue().get(redisKey);

        if (cachedValue == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        OtpData otpData = objectMapper.convertValue(cachedValue, OtpData.class);

        if (otpData.getAttempts() >= MAX_ATTEMPTS) {
            throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }

        if (!passwordEncoder.matches(rawOtp, otpData.getOtpHash())) {
            otpData.setAttempts(otpData.getAttempts() + 1);
            Long remainingTtlSeconds = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

            if (remainingTtlSeconds != null && remainingTtlSeconds > 0) {
                redisTemplate.opsForValue().set(redisKey, otpData, Duration.ofSeconds(remainingTtlSeconds));
            }

            if (otpData.getAttempts() >= MAX_ATTEMPTS) {
                throw new AppException(ErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
            }
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        // Xác thực thành công: xóa OTP khỏi Redis
        redisTemplate.delete(redisKey);

        log.info("OTP verified successfully in Redis for user [{}] with purpose [{}]", normalizedEmail, purpose);
        return user;
    }

    private String buildKey(OtpPurpose purpose, String email) {
        return String.format(REDIS_OTP_PREFIX, purpose.name(), email);
    }
}
