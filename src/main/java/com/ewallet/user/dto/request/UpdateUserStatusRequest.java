package com.ewallet.user.dto.request;

import com.ewallet.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
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
public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái người dùng không được để trống")
    private UserStatus status;
}
