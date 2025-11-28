package com.example.validation.controller;

import com.example.validation.model.dto.request.UserDataTransfer;
import com.example.validation.model.dto.request.UserRegistrationRequest;
import com.example.validation.model.dto.request.UserVipRequest;
import com.example.validation.model.dto.response.UserResponse;
import com.example.validation.service.ProfileService;
import com.example.validation.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 使用者 Controller
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;

    /**
     * 使用者註冊 API
     *
     * 重點：@Valid 註解會觸發 Bean Validation
     * 如果驗證失敗，Spring 會自動拋出 MethodArgumentNotValidException
     * 該異常會被 GlobalExceptionHandler 捕獲並處理
     *
     * @param request 使用者註冊請求（會自動驗證）
     * @return 註冊成功的使用者資訊
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 更新使用者個人資料 API
     *
     * 展示情境：
     * 1. Controller 先從 UserService 取得使用者資料
     * 2. 將資料包裝成 UserDataTransfer（帶有驗證註解）
     * 3. 傳入 ProfileService，Service 層會自動驗證資料完整性
     *
     * 重點：即使資料來自內部 Service，仍可確保資料的完整性
     *
     * @param userId 使用者 ID
     * @param newPhone 新的電話號碼
     * @return 更新結果訊息
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<String> updateProfile(
            @PathVariable Long userId,
            @RequestParam String newPhone) {

        // 步驟 1: 從 UserService 取得使用者資料
        UserDataTransfer userData = userService.getUserData(userId);

        // 步驟 2: 將資料傳入 ProfileService
        // ProfileService 的方法參數有 @Valid，會自動驗證 userData
        // 如果 userData 的任何必要欄位為 null，會拋出 ConstraintViolationException
        String result = profileService.updateProfile(userData, newPhone);

        return ResponseEntity.ok(result);
    }

    /**
     * 測試 Record 配合 @AssertTrue 的 API
     *
     * 展示：Record 如何使用 @AssertTrue 進行複雜驗證
     *
     * @param request Record 格式的請求 DTO（包含 @AssertTrue 驗證）
     * @return 驗證成功訊息
     */
    @PostMapping("/vip/validate")
    public ResponseEntity<String> validateVip(@Valid @RequestBody UserVipRequest request) {
        // @Valid 會自動觸發 @AssertTrue 驗證
        // 驗證規則：
        // 1. isValidVipDiscount() - VIP 等級與折扣率必須對應
        // 2. isValidPlatinumAge() - 白金卡必須年滿 30 歲
        // 3. isValidDiscountRange() - 折扣率必須在 0-100 之間

        return ResponseEntity.ok(
            String.format("驗證通過！使用者: %s, VIP等級: %d, 折扣率: %d%%",
                request.name(),
                request.vipLevel(),
                request.discountRate())
        );
    }
}
