package com.example.validation.model.dto.response;

import java.time.LocalDateTime;

/**
 * 使用者回應 DTO
 */
public record UserResponse(
    Long id,
    String name,
    String email,
    Integer age,
    LocalDateTime createdAt
) {
}
