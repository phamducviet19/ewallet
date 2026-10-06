package com.ewallet.common.service;

import com.ewallet.common.dto.IdempotencyRecord;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyService {

    boolean lock(UUID userId, String key, String requestHash, Duration ttl);

    Optional<IdempotencyRecord> get(UUID userId, String key);

    void markSuccess(UUID userId, String key, String requestHash, int statusCode, String responseBody, Duration ttl);

    void markFailed(UUID userId, String key, String requestHash, int statusCode, String responseBody, Duration ttl);

    void unlock(UUID userId, String key);
}
