package com.example.validation.controller;

import com.example.validation.model.dto.request.UserRegistrationRequest;
import com.example.validation.model.dto.response.UserResponse;
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
}
