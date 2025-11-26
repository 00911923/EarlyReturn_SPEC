package com.example.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Early Return 優化範例應用程式
 *
 * 本專案展示如何從傳統的 Early Return 驗證方式，
 * 改進為使用 Bean Validation 的優雅做法
 */
@SpringBootApplication
public class ValidationDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationDemoApplication.class, args);
    }
}
