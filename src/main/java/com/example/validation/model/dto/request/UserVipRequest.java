package com.example.validation.model.dto.request;

import jakarta.validation.constraints.*;

/**
 * 使用 Record 配合 @AssertTrue 的範例
 *
 * Record 的優點：
 * 1. 簡潔的語法
 * 2. 自動生成 constructor、getter、equals、hashCode、toString
 * 3. 不可變（immutable）
 * 4. 可以添加自定義方法（包括 @AssertTrue 驗證方法）
 */
public record UserVipRequest(

        @NotNull(message = "使用者 ID 不可為空")
        Long userId,

        @NotBlank(message = "姓名不可為空")
        @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
        String name,

        @NotNull(message = "年齡不可為空")
        @Min(value = 18, message = "年齡必須大於等於 18 歲")
        @Max(value = 150, message = "年齡必須小於 150 歲")
        Integer age,

        Integer vipLevel,      // VIP 等級：0=普通, 1=銀卡, 2=金卡, 3=白金卡
        Integer discountRate   // 折扣率（百分比）
) {

    /**
     * @AssertTrue 驗證方法
     *
     * 重點：
     * 1. Record 可以定義自定義方法
     * 2. 方法名必須以 is 開頭，返回 boolean
     * 3. 可以訪問 record 的所有欄位（直接使用欄位名，不需要 this.）
     *
     * 業務規則：VIP 等級與折扣率必須對應
     * - 普通會員 (0): 無折扣 (0%)
     * - 銀卡 (1): 5-10%
     * - 金卡 (2): 10-20%
     * - 白金卡 (3): 20-30%
     */
    @AssertTrue(message = "VIP 等級與折扣率不符合規則")
    public boolean isValidVipDiscount() {
        // Record 的欄位可以直接訪問（不需要 this.vipLevel）
        if (vipLevel == null || discountRate == null) {
            return true;  // 允許為空
        }

        return switch (vipLevel) {
            case 0 -> discountRate == 0;
            case 1 -> discountRate >= 5 && discountRate <= 10;
            case 2 -> discountRate >= 10 && discountRate <= 20;
            case 3 -> discountRate >= 20 && discountRate <= 30;
            default -> false;
        };
    }

    /**
     * 白金卡年齡限制驗證
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
     * 折扣率範圍驗證
     *
     * 業務規則：折扣率必須在 0-100 之間
     */
    @AssertTrue(message = "折扣率必須在 0-100 之間")
    public boolean isValidDiscountRange() {
        if (discountRate == null) {
            return true;
        }

        return discountRate >= 0 && discountRate <= 100;
    }

    /**
     * 工廠方法：從 Entity 創建
     *
     * Record 可以定義靜態方法作為工廠方法
     */
    public static UserVipRequest fromEntity(
            com.example.validation.model.entity.User user,
            Integer vipLevel,
            Integer discountRate) {
        return new UserVipRequest(
            user.getId(),
            user.getName(),
            user.getAge(),
            vipLevel,
            discountRate
        );
    }

    /**
     * Compact Constructor（緊湊建構子）
     *
     * Record 特有的功能，可以在建構時進行額外的驗證或處理
     * 注意：這裡的驗證在物件建立時執行，不是 Bean Validation
     */
    public UserVipRequest {
        // 可以在這裡進行一些基本的檢查或資料處理
        // 例如：將 null 的 vipLevel 設為預設值 0
        if (vipLevel == null) {
            vipLevel = 0;  // 預設為普通會員
        }
        if (discountRate == null) {
            discountRate = 0;  // 預設無折扣
        }
    }
}
