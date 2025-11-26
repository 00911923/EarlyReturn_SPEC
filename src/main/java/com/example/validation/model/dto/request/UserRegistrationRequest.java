package com.example.validation.model.dto.request;

import com.example.validation.validation.UniqueEmail;
import jakarta.validation.constraints.*;

/**
 * 使用者註冊請求 DTO
 *
 * 使用 Bean Validation 註解進行聲明式驗證
 */
public record UserRegistrationRequest(

    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
    String name,

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @UniqueEmail(message = "Email 已被註冊")
    String email,

    @NotNull(message = "年齡不可為空")
    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    @Max(value = 150, message = "年齡必須小於 150 歲")
    Integer age,

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼長度必須至少 8 個字元")
    String password
) {
}
