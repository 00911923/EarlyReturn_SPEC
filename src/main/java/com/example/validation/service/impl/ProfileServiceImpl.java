package com.example.validation.service.impl;

import com.example.validation.model.dto.request.UserDataTransfer;
import com.example.validation.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 個人資料服務實作
 *
 * 重點：
 * 1. 類別加上 @Validated 註解，啟用 Service 層驗證
 * 2. @Valid 註解定義在介面 ProfileService 上，而不是實作類別上
 * 3. Spring 會自動驗證傳入的物件
 * 4. 驗證失敗會拋出 ConstraintViolationException
 */
@Service
@Validated  // ← 重點：啟用 Service 層的驗證功能
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    /**
     * 更新使用者個人資料
     *
     * 注意：@Valid 註解定義在介面 ProfileService 上
     * 實作類別不需要重複定義 @Valid，否則會違反 Bean Validation 規範
     *
     * @param userData 從 UserService 取得的使用者資料
     *                 即使資料來自內部 Service，@Valid 仍會驗證所有欄位
     * @param newPhone 新的電話號碼
     * @return 更新後的訊息
     */
    @Override
    public String updateProfile(UserDataTransfer userData, String newPhone) {

        // 此時 userData 已經通過驗證，確保所有必要欄位都有值
        log.info("更新使用者資料 - ID: {}, Name: {}, Email: {}, Age: {}",
                userData.getUserId(),
                userData.getName(),
                userData.getEmail(),
                userData.getAge());

        log.info("新電話號碼: {}", newPhone);

        // 實際業務邏輯：更新資料庫等
        // ... 這裡可以安全地使用 userData 的所有欄位

        return String.format("成功更新使用者 %s 的資料，新電話: %s",
                userData.getName(), newPhone);
    }
}
