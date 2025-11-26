# Early Return 優化範例專案

本專案展示如何從傳統的 Early Return 驗證方式，改進為使用 Bean Validation 的優雅做法。

## 專案目的

展示在 Spring Boot 專案中，如何優雅地處理參數驗證，從手動的 if-else 驗證改為聲明式的 Bean Validation。

## 改進前後對比

### ❌ 改進前：使用 Early Return

```java
@Service
public class UserServiceOldWay {

    public UserResponse registerUser(UserRegistrationRequest request) {
        // 大量的 if 判斷
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("姓名不可為空");
        }

        if (request.getName().length() < 2 || request.getName().length() > 50) {
            throw new ValidationException("姓名長度必須在 2-50 字元之間");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email 不可為空");
        }

        if (!isValidEmail(request.getEmail())) {
            throw new ValidationException("Email 格式不正確");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email 已被註冊");
        }

        if (request.getAge() == null) {
            throw new ValidationException("年齡不可為空");
        }

        if (request.getAge() < 18) {
            throw new ValidationException("年齡必須大於等於 18 歲");
        }

        if (request.getAge() > 150) {
            throw new ValidationException("年齡必須小於 150 歲");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ValidationException("密碼長度必須至少 8 個字元");
        }

        // 終於可以執行業務邏輯了...
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private boolean isValidEmail(String email) {
        // 自己寫 email 驗證邏輯...
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

**問題**：
- 程式碼冗長，難以維護
- 驗證邏輯與業務邏輯混在一起
- 每個欄位都要手動檢查
- 錯誤訊息分散在各處
- 難以重用驗證邏輯

---

### ✅ 改進後：使用 Bean Validation

#### 1. DTO 使用聲明式驗證

```java
public record UserRegistrationRequest(
    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
    String name,

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @UniqueEmail(message = "Email 已被註冊")  // 自定義驗證
    String email,

    @NotNull(message = "年齡不可為空")
    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    @Max(value = 150, message = "年齡必須小於 150 歲")
    Integer age,

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼長度必須至少 8 個字元")
    String password
) {}
```

#### 2. Service 層簡潔清晰

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(@Valid UserRegistrationRequest request) {
        // 驗證已經在 DTO 層完成！
        // 這裡只需要專注於業務邏輯

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);
        return toResponse(saved);
    }
}
```

**優點**：
- ✅ 程式碼簡潔易讀
- ✅ 驗證邏輯與業務邏輯分離
- ✅ 使用標準的 Bean Validation 註解
- ✅ 驗證邏輯可重用
- ✅ 錯誤訊息集中管理
- ✅ 支援自定義驗證器

---

## 專案結構

```
earlyReturnOptimize/
├── pom.xml                                          # Maven 配置
├── README.md                                        # 本檔案
└── src/main/
    ├── java/com/example/validation/
    │   ├── ValidationDemoApplication.java           # Spring Boot 主程式
    │   ├── controller/
    │   │   └── UserController.java                  # REST API 端點
    │   ├── service/
    │   │   ├── UserService.java                     # Service 介面
    │   │   └── impl/
    │   │       └── UserServiceImpl.java             # Service 實作
    │   ├── model/
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   └── UserRegistrationRequest.java # 請求 DTO (含驗證註解)
    │   │   │   └── response/
    │   │   │       ├── UserResponse.java            # 回應 DTO
    │   │   │       └── ErrorResponse.java           # 錯誤回應 DTO
    │   │   └── entity/
    │   │       └── User.java                        # JPA 實體
    │   ├── repository/
    │   │   └── UserRepository.java                  # Spring Data JPA Repository
    │   ├── exception/
    │   │   ├── BusinessException.java               # 業務異常
    │   │   └── handler/
    │   │       └── GlobalExceptionHandler.java      # 全域異常處理
    │   └── validation/
    │       ├── UniqueEmail.java                     # 自定義驗證註解
    │       └── UniqueEmailValidator.java            # 自定義驗證器實作
    └── resources/
        └── application.yml                          # Spring Boot 配置
```

---

## 核心技術

### 1. 標準 Bean Validation 註解

| 註解 | 用途 |
|------|------|
| `@NotNull` | 欄位不可為 null |
| `@NotBlank` | 字串不可為空白（自動 trim） |
| `@Size(min, max)` | 字串長度或集合大小限制 |
| `@Min(value)` | 數字最小值 |
| `@Max(value)` | 數字最大值 |
| `@Email` | Email 格式驗證 |
| `@Pattern(regexp)` | 正規表達式驗證 |
| `@Valid` | 觸發驗證（在方法參數或欄位上） |

### 2. 自定義驗證器

#### 步驟 1：建立自定義註解

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email 已被註冊";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### 步驟 2：實作驗證器

```java
@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;  // null 由 @NotBlank 處理
        }
        return !userRepository.existsByEmail(email);
    }
}
```

### 3. 全域異常處理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("驗證失敗", errors));
    }
}
```

---

## API 端點

### 使用者註冊

**請求**：
```http
POST /api/users/register
Content-Type: application/json

{
  "name": "張三",
  "email": "zhangsan@example.com",
  "age": 25,
  "password": "securePassword123"
}
```

**成功回應**：
```json
{
  "id": 1,
  "name": "張三",
  "email": "zhangsan@example.com",
  "age": 25,
  "createdAt": "2025-01-15T10:30:00"
}
```

**驗證失敗回應**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "email": "Email 已被註冊",
    "age": "年齡必須大於等於 18 歲"
  }
}
```

---

## 如何執行

### 1. 編譯專案

```bash
mvn clean package
```

### 2. 執行專案

```bash
mvn spring-boot:run
```

或直接執行 JAR：

```bash
java -jar target/validation-demo-1.0.0.jar
```

### 3. 測試 API

使用 curl 測試：

```bash
# 成功案例
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "張三",
    "email": "zhangsan@example.com",
    "age": 25,
    "password": "securePassword123"
  }'

# 驗證失敗案例 - Email 格式錯誤
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "李四",
    "email": "invalid-email",
    "age": 30,
    "password": "password123"
  }'

# 驗證失敗案例 - 年齡未滿 18 歲
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "王五",
    "email": "wangwu@example.com",
    "age": 16,
    "password": "password123"
  }'
```

---

## 學習重點

### 1. 聲明式驗證 vs 命令式驗證

| 特性 | Early Return (命令式) | Bean Validation (聲明式) |
|------|---------------------|------------------------|
| 程式碼長度 | 冗長 | 簡潔 |
| 可讀性 | 較差 | 優秀 |
| 維護性 | 困難 | 容易 |
| 重用性 | 低 | 高 |
| 關注點分離 | 混雜 | 清晰 |

### 2. 驗證的層次

```
Controller 層 (@Valid)
    ↓
DTO 層 (Bean Validation 註解)
    ↓
自定義 Validator (業務邏輯驗證)
    ↓
Service 層 (複雜業務邏輯驗證)
```

### 3. 何時使用不同的驗證方式

- **基本欄位驗證**：使用標準 Bean Validation 註解
- **格式驗證**：使用 `@Email`、`@Pattern` 等
- **業務規則驗證**：使用自定義 Validator（如 `@UniqueEmail`）
- **複雜業務邏輯**：在 Service 層手動驗證

---

## 延伸學習

### 1. Validation Groups（驗證組）

用於不同情境使用不同驗證規則：

```java
public interface CreateGroup {}
public interface UpdateGroup {}

public record UserRequest(
    @Null(groups = CreateGroup.class)  // 新增時 ID 必須為 null
    @NotNull(groups = UpdateGroup.class)  // 更新時 ID 不可為 null
    Long id,

    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    String name
) {}
```

### 2. 自定義驗證訊息國際化

在 `messages.properties` 中定義訊息：

```properties
user.email.unique=此 Email 已被註冊
user.age.min=年齡必須大於等於 {value} 歲
```

### 3. 使用 `@Validated` 進行 Service 層驗證

```java
@Service
@Validated
public class UserServiceImpl {

    public void updateUser(@NotNull Long id, @Valid UserUpdateRequest request) {
        // Spring 會自動驗證參數
    }
}
```

---

## 總結

透過本專案，您可以學習到：

✅ Bean Validation 的基本使用
✅ 自定義驗證器的實作
✅ 全域異常處理的設計
✅ 如何優雅地處理參數驗證
✅ 關注點分離的最佳實踐

**核心理念**：讓驗證邏輯「看起來像配置，而不是程式碼」。

---

## 參考資源

- [Bean Validation 官方文檔](https://beanvalidation.org/)
- [Spring Boot Validation 指南](https://spring.io/guides/gs/validating-form-input/)
- [Hibernate Validator 文檔](https://hibernate.org/validator/)
