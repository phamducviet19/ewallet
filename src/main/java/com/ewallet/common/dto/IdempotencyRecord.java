package com.ewallet.common.dto;

import com.ewallet.common.entity.IdempotencyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord implements Serializable {

    private String key;
    private String requestHash;
    private IdempotencyStatus status;
    private Integer responseStatus;
    private String responseBody;
    private LocalDateTime createdAt;
}
