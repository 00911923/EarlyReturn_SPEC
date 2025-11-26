package com.example.validation.service.impl;

import com.example.validation.model.dto.request.UserRegistrationRequest;
import com.example.validation.model.dto.response.UserResponse;
import com.example.validation.model.entity.User;
import com.example.validation.repository.UserRepository;
import com.example.validation.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 使用者服務實作
 *
 * 展示使用 Bean Validation 後，Service 層的程式碼變得多麼簡潔！
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * 註冊新使用者
     *
     * 注意：此方法不需要任何驗證邏輯！
     * 所有基本驗證都已經在 DTO 層透過 Bean Validation 完成
     *
     * @param request 使用者註冊請求（已驗證）
     * @return 註冊成功的使用者資訊
     */
    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("註冊新使用者: {}", request.email());

        // 建立使用者實體
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());
        // 實際專案中應該使用 PasswordEncoder 加密密碼
        user.setPassword(request.password());

        // 儲存到資料庫
        User savedUser = userRepository.save(user);

        log.info("使用者註冊成功，ID: {}", savedUser.getId());

        // 轉換為回應 DTO
        return toResponse(savedUser);
    }

    /**
     * 將 User 實體轉換為 UserResponse DTO
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.getCreatedAt()
        );
    }
}
