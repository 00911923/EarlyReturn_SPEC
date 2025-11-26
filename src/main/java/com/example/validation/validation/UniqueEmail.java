package com.example.validation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定義驗證註解：檢查 Email 是否唯一
 *
 * 用法範例：
 * <pre>
 * public record UserRequest(
 *     @UniqueEmail(message = "Email 已被註冊")
 *     String email
 * ) {}
 * </pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {

    /**
     * 驗證失敗時的錯誤訊息
     */
    String message() default "Email 已被註冊";

    /**
     * 驗證組（用於分組驗證）
     */
    Class<?>[] groups() default {};

    /**
     * 附加資訊（用於攜帶元數據）
     */
    Class<? extends Payload>[] payload() default {};
}
