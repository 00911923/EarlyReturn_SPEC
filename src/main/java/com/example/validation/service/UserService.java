package com.example.validation.service;

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
}
