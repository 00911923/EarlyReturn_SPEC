package com.example.validation.model.dto.response;

import java.util.Map;

/**
 * 錯誤回應 DTO
 */
public record ErrorResponse(
    String message,
    Map<String, String> errors
) {
}
