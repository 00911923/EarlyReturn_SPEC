package com.example.validation.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用者更新請求 DTO
 *
 * 展示如何在 DTO 中使用 @AssertTrue 進行複雜驗證
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

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

    // 新增欄位：VIP 等級和折扣
    private Integer vipLevel;      // 0=普通, 1=銀卡, 2=金卡, 3=白金卡
    private Integer discountRate;  // 折扣率（百分比）

    /**
     * 自定義驗證：VIP 等級和折扣率的對應關係
     *
     * @AssertTrue 會檢查這個方法的返回值是否為 true
     * 方法名必須以 is 開頭
     *
     * 業務規則：
     * - 普通會員 (level 0): 折扣 0%
     * - 銀卡 (level 1): 折扣 5-10%
     * - 金卡 (level 2): 折扣 10-20%
     * - 白金卡 (level 3): 折扣 20-30%
     */
    @AssertTrue(message = "VIP 等級與折扣率不符合規則")
    public boolean isValidVipDiscount() {
        // 如果沒有設定 VIP 等級，不驗證
        if (vipLevel == null || discountRate == null) {
            return true;
        }

        // 根據 VIP 等級檢查折扣率範圍
        return switch (vipLevel) {
            case 0 -> discountRate == 0;                          // 普通會員無折扣
            case 1 -> discountRate >= 5 && discountRate <= 10;    // 銀卡 5-10%
            case 2 -> discountRate >= 10 && discountRate <= 20;   // 金卡 10-20%
            case 3 -> discountRate >= 20 && discountRate <= 30;   // 白金卡 20-30%
            default -> false;                                      // 無效的 VIP 等級
        };
    }

    /**
     * 自定義驗證：年齡與 VIP 等級的關係
     *
     * 業務規則：白金卡僅限 30 歲以上會員
     */
    @AssertTrue(message = "白金卡僅限 30 歲以上會員申請")
    public boolean isValidPlatinumAge() {
        if (vipLevel == null || age == null) {
            return true;
        }

        // 白金卡必須年滿 30 歲
        if (vipLevel == 3) {
            return age >= 30;
        }

        return true;
    }

    /**
     * 從 Entity 轉換（工廠方法）
     */
    public static UserUpdateRequest fromEntity(
            com.example.validation.model.entity.User user,
            Integer vipLevel,
            Integer discountRate) {
        return new UserUpdateRequest(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            vipLevel,
            discountRate
        );
    }
}
