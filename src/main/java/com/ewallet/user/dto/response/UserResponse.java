package com.ewallet.user.dto.response;

import com.ewallet.user.entity.Role;
import com.ewallet.user.entity.User;
import com.ewallet.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private UserStatus status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(user.getRoles() != null
                        ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                        : Set.of())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
