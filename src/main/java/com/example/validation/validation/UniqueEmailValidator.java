package com.example.validation.validation;

import com.example.validation.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UniqueEmail 驗證器實作
 *
 * 負責檢查 Email 是否已存在於資料庫中
 */
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 驗證 Email 是否唯一
     *
     * @param email Email 地址
     * @param context 驗證上下文
     * @return true 表示驗證通過（Email 不存在），false 表示驗證失敗（Email 已存在）
     */
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // 如果 email 為 null，由 @NotBlank 處理
        if (email == null) {
            return true;
        }

        // 檢查資料庫中是否已存在此 Email
        return !userRepository.existsByEmail(email);
    }
}
