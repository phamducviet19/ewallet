package com.ewallet.user.security;

import com.ewallet.common.exception.AppException;
import com.ewallet.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserDetails().getEmail();
    }
}
