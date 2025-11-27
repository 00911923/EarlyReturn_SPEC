package com.example.validation.service;

import com.example.validation.model.dto.request.UserDataTransfer;
import com.example.validation.model.dto.request.UserRegistrationRequest;
import com.example.validation.model.dto.response.UserResponse;

/**
 * 使用者服務介面
 */
public interface UserService {

    /**
     * 註冊新使用者
     *
     * @param request 使用者註冊請求
     * @return 註冊成功的使用者資訊
     */
    UserResponse registerUser(UserRegistrationRequest request);

    /**
     * 根據 ID 取得使用者資料
     *
     * @param userId 使用者 ID
     * @return 使用者資料傳輸物件
     */
    UserDataTransfer getUserData(Long userId);
}
