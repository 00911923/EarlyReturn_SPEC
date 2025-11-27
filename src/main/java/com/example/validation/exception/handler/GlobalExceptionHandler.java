package com.example.validation.exception.handler;

import com.example.validation.exception.BusinessException;
import com.example.validation.model.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
     * 處理 Controller 層 Bean Validation 驗證失敗的異常
     *
     * 當 Controller 的 @Valid 驗證失敗時，Spring 會拋出 MethodArgumentNotValidException
     * 我們在這裡捕獲並轉換為友善的錯誤訊息
     *
     * @param ex 驗證異常
     * @return 錯誤回應
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.warn("Controller 層驗證失敗: {}", ex.getMessage());

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
     * 處理 Service 層 Bean Validation 驗證失敗的異常
     *
     * 當 Service 的 @Valid 驗證失敗時，Spring 會拋出 ConstraintViolationException
     * 這通常發生在 Service 層方法參數使用 @Valid 註解的情況
     *
     * @param ex 約束違反異常
     * @return 錯誤回應
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {

        log.warn("Service 層驗證失敗: {}", ex.getMessage());

        // 收集所有驗證錯誤
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            // 取得欄位名稱（去除方法名稱前綴）
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(fieldName, violation.getMessage());
        }

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

        // 強制輸出到控制台（因為日誌系統可能有問題）
        System.err.println("========== 系統異常 ==========");
        System.err.println("異常類型: " + ex.getClass().getName());
        System.err.println("異常訊息: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("==============================");

        ErrorResponse errorResponse = new ErrorResponse(
            "系統發生錯誤，請稍後再試",
            Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
