package com.ewallet.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserResponse user;
}
