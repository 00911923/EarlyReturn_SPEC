package com.example.validation.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用於 Service 之間傳遞的使用者資料 DTO
 *
 * 使用情境：
 * 1. Controller 從 Service A 取得資料
 * 2. 將資料包裝成此 DTO
 * 3. 傳入 Service B 並進行驗證
 *
 * 重點：即使資料來自內部 Service，仍然可以進行驗證確保資料完整性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataTransfer {

    @NotNull(message = "使用者 ID 不可為空")
    private Long userId;

    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
    private String name;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotNull(message = "年齡不可為空")
    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    @Max(value = 150, message = "年齡必須小於 150 歲")
    private Integer age;

    /**
     * 從 User Entity 轉換
     */
    public static UserDataTransfer fromUser(com.example.validation.model.entity.User user) {
        if (user == null) {
            return null;
        }
        return new UserDataTransfer(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge()
        );
    }
}
