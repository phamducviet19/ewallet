package com.ewallet.user.service.impl;

import com.ewallet.common.dto.response.PageResponse;
import com.ewallet.common.exception.AppException;
import com.ewallet.common.exception.ErrorCode;
import com.ewallet.user.dto.request.UpdateProfileRequest;
import com.ewallet.user.dto.response.UserResponse;
import com.ewallet.user.entity.User;
import com.ewallet.user.entity.UserStatus;
import com.ewallet.user.repository.UserRepository;
import com.ewallet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (StringUtils.hasText(request.getPhone())) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone)) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
            user.setPhone(newPhone);
        } else {
            user.setPhone(null);
        }

        user.setFullName(request.getFullName().trim());
        User updatedUser = userRepository.save(user);

        log.info("Updated profile for user [{}]", user.getEmail());
        return UserResponse.from(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(String search, UserStatus status, Pageable pageable) {
        String cleanSearch = StringUtils.hasText(search) ? search.trim() : null;
        Page<User> usersPage = userRepository.searchUsers(cleanSearch, status, pageable);
        return PageResponse.from(usersPage.map(UserResponse::from));
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        log.info("Admin updated user [{}] status to [{}]", user.getEmail(), status);
        return UserResponse.from(updatedUser);
    }
}
