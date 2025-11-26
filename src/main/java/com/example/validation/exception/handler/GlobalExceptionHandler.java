package com.example.validation.exception.handler;

import com.example.validation.exception.BusinessException;
import com.example.validation.model.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域異常處理器
 *
 * 統一處理所有的異常，並返回友善的錯誤訊息
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 處理 Bean Validation 驗證失敗的異常
     *
     * 當 @Valid 驗證失敗時，Spring 會拋出 MethodArgumentNotValidException
     * 我們在這裡捕獲並轉換為友善的錯誤訊息
     *
     * @param ex 驗證異常
     * @return 錯誤回應
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.warn("驗證失敗: {}", ex.getMessage());

        // 收集所有欄位的驗證錯誤
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = new ErrorResponse("驗證失敗", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 處理業務異常
     *
     * @param ex 業務異常
     * @return 錯誤回應
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("業務異常: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 處理其他未預期的異常
     *
     * @param ex 異常
     * @return 錯誤回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("系統異常: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            "系統發生錯誤，請稍後再試",
            Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
