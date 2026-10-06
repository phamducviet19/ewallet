package com.ewallet.common.service.impl;

import com.ewallet.common.dto.IdempotencyRecord;
import com.ewallet.common.entity.IdempotencyStatus;
import com.ewallet.common.service.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:%s:%s";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean lock(UUID userId, String key, String requestHash, Duration ttl) {
        String redisKey = buildKey(userId, key);
        IdempotencyRecord record = IdempotencyRecord.builder()
                .key(key)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .build();

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, record, ttl);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public Optional<IdempotencyRecord> get(UUID userId, String key) {
        String redisKey = buildKey(userId, key);
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.convertValue(value, IdempotencyRecord.class));
    }

    @Override
    public void markSuccess(UUID userId, String key, String requestHash, int statusCode, String responseBody, Duration ttl) {
        String redisKey = buildKey(userId, key);
        IdempotencyRecord record = IdempotencyRecord.builder()
                .key(key)
                .requestHash(requestHash)
                .status(IdempotencyStatus.COMPLETED)
                .responseStatus(statusCode)
                .responseBody(responseBody)
                .createdAt(LocalDateTime.now())
                .build();

        redisTemplate.opsForValue().set(redisKey, record, ttl);
        log.debug("Marked idempotency key [{}] as SUCCESS for user [{}]", key, userId);
    }

    @Override
    public void markFailed(UUID userId, String key, String requestHash, int statusCode, String responseBody, Duration ttl) {
        String redisKey = buildKey(userId, key);
        IdempotencyRecord record = IdempotencyRecord.builder()
                .key(key)
                .requestHash(requestHash)
                .status(IdempotencyStatus.FAILED)
                .responseStatus(statusCode)
                .responseBody(responseBody)
                .createdAt(LocalDateTime.now())
                .build();

        redisTemplate.opsForValue().set(redisKey, record, ttl);
        log.debug("Marked idempotency key [{}] as FAILED for user [{}]", key, userId);
    }

    @Override
    public void unlock(UUID userId, String key) {
        String redisKey = buildKey(userId, key);
        redisTemplate.delete(redisKey);
        log.debug("Unlocked idempotency key [{}] for user [{}]", key, userId);
    }

    private String buildKey(UUID userId, String key) {
        return String.format(IDEMPOTENCY_KEY_PREFIX, userId, key);
    }
}
