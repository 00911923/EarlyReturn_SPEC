# Bean Validation 完整範例專案

本專案展示如何從傳統的 Early Return 驗證方式，改進為使用 Bean Validation 的優雅做法，包含：

- ✅ **Controller 層驗證**：使用 `@Valid` + Request DTO
- ✅ **Service 層驗證**：跨 Service 的資料完整性驗證
- ✅ **Record + @AssertTrue**：使用 Java Record 進行複雜業務邏輯驗證
- ✅ **自定義驗證器**：實作資料庫相關的驗證邏輯
- ✅ **全域異常處理**：統一的錯誤回應格式

---

## 📚 目錄

1. [專案目的](#專案目的)
2. [改進前後對比](#改進前後對比)
3. [專案結構](#專案結構)
4. [核心技術](#核心技術)
5. [API 端點](#api-端點)
6. [如何執行與測試](#如何執行與測試)
7. [學習重點](#學習重點)
8. [延伸閱讀](#延伸閱讀)

---

## 專案目的

展示在 Spring Boot 專案中，如何優雅地處理參數驗證，涵蓋三個層級：

1. **Controller 層驗證**：外部輸入的基本驗證
2. **Service 層驗證**：內部服務間傳遞的資料完整性驗證
3. **複雜業務邏輯驗證**：使用 `@AssertTrue` 進行跨欄位驗證

---

## 改進前後對比

### ❌ 改進前：使用 Early Return

```java
@Service
public class UserServiceOldWay {

    public UserResponse registerUser(UserRegistrationRequest request) {
        // 大量的 if 判斷，業務邏輯被淹沒
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("姓名不可為空");
        }

        if (request.getName().length() < 2 || request.getName().length() > 50) {
            throw new ValidationException("姓名長度必須在 2-50 字元之間");
        }

        if (request.getEmail() == null || !isValidEmail(request.getEmail())) {
            throw new ValidationException("Email 格式不正確");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email 已被註冊");
        }

        if (request.getAge() == null || request.getAge() < 18 || request.getAge() > 150) {
            throw new ValidationException("年齡必須在 18-150 之間");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ValidationException("密碼長度必須至少 8 個字元");
        }

        // 終於可以執行業務邏輯了... (已經 20 多行了)
        User user = new User();
        user.setName(request.getName());
        // ...
    }
}
```

**問題：**
- ❌ 程式碼冗長，難以維護
- ❌ 驗證邏輯與業務邏輯混在一起
- ❌ 每個欄位都要手動檢查
- ❌ 錯誤訊息分散在各處
- ❌ 難以重用驗證邏輯

---

### ✅ 改進後：使用 Bean Validation

#### 1. DTO 使用聲明式驗證（Record 格式）

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

    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {
        // 驗證已經在 DTO 層完成！這裡只需要專注於業務邏輯

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());
        user.setPassword(request.password());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }
}
```

**優點：**
- ✅ 程式碼簡潔易讀（減少 80%+ 程式碼）
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
├── SERVICE_LAYER_VALIDATION.md                      # Service 層驗證指南
├── RECORD_VS_CLASS_VALIDATION.md                    # Record vs Class 對比
├── DTO_VALIDATION_GUIDE.md                          # DTO 驗證與資料轉換指南
├── POSTMAN_TEST_GUIDE.md                            # Postman 測試指南
├── RECORD_TEST_GUIDE.md                             # Record 測試指南
├── api-tests.http                                   # Controller 層測試（IntelliJ）
├── api-tests-profile.http                           # Service 層測試（IntelliJ）
├── api-tests-record.http                            # Record 測試（IntelliJ）
└── src/main/
    ├── java/com/example/validation/
    │   ├── ValidationDemoApplication.java           # Spring Boot 主程式
    │   ├── controller/
    │   │   └── UserController.java                  # REST API 端點（3 個 API）
    │   ├── service/
    │   │   ├── UserService.java                     # 使用者服務介面
    │   │   ├── ProfileService.java                  # 個人資料服務介面（⭐ Service 層驗證）
    │   │   └── impl/
    │   │       ├── UserServiceImpl.java             # 使用者服務實作
    │   │       └── ProfileServiceImpl.java          # 個人資料服務實作（@Validated）
    │   ├── model/
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── UserRegistrationRequest.java # 註冊 DTO（Record）
    │   │   │   │   ├── UserDataTransfer.java        # Service 間傳遞 DTO（Class）
    │   │   │   │   ├── UserVipRequest.java          # ⭐ Record + @AssertTrue 範例
    │   │   │   │   └── UserUpdateRequest.java       # Class + @AssertTrue 範例
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
        ├── application.yml                          # Spring Boot 配置
        └── logback.xml                              # 日誌配置
```

---

## 核心技術

### 1. Controller 層驗證（基本）

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        // @Valid 觸發 DTO 的驗證
        // 驗證失敗拋出 MethodArgumentNotValidException
        // 由 GlobalExceptionHandler 統一處理
    }
}
```

### 2. Service 層驗證（進階 ⭐）

展示如何驗證從一個 Service 取得並傳遞給另一個 Service 的資料：

```java
// 介面：定義驗證約束
public interface ProfileService {
    String updateProfile(@Valid @NotNull UserDataTransfer userData, String newPhone);
}

// 實作：啟用驗證
@Service
@Validated  // ← 必須加這個才能啟用 Service 層驗證
public class ProfileServiceImpl implements ProfileService {

    @Override
    public String updateProfile(UserDataTransfer userData, String newPhone) {
        // userData 已經通過驗證，所有欄位都確保有值
        // 驗證失敗拋出 ConstraintViolationException
    }
}
```

**關鍵要點：**
- `@Validated` 加在實作類別上（啟用 Spring AOP 驗證）
- `@Valid` 定義在介面方法參數上（聲明驗證約束）
- 實作類別不可重複定義 `@Valid`（Bean Validation 規範）

### 3. Record + @AssertTrue（複雜業務邏輯驗證）

使用 Java Record 配合 `@AssertTrue` 進行跨欄位的複雜驗證：

```java
public record UserVipRequest(
        @NotNull Long userId,
        @NotBlank String name,
        @Min(18) Integer age,
        Integer vipLevel,      // 0=普通, 1=銀卡, 2=金卡, 3=白金卡
        Integer discountRate   // 折扣率（百分比）
) {
    /**
     * @AssertTrue 驗證方法
     * 業務規則：VIP 等級與折扣率必須對應
     */
    @AssertTrue(message = "VIP 等級與折扣率不符合規則")
    public boolean isValidVipDiscount() {
        if (vipLevel == null || discountRate == null) {
            return true;
        }

        return switch (vipLevel) {
            case 0 -> discountRate == 0;      // 普通會員無折扣
            case 1 -> discountRate >= 5 && discountRate <= 10;  // 銀卡 5-10%
            case 2 -> discountRate >= 10 && discountRate <= 20; // 金卡 10-20%
            case 3 -> discountRate >= 20 && discountRate <= 30; // 白金卡 20-30%
            default -> false;
        };
    }

    @AssertTrue(message = "白金卡僅限 30 歲以上會員")
    public boolean isValidPlatinumAge() {
        if (vipLevel == null || age == null) {
            return true;
        }
        return vipLevel != 3 || age >= 30;  // 白金卡必須年滿 30 歲
    }
}
```

**Record 的優勢：**
- ✅ 語法簡潔（不需要 Lombok）
- ✅ 不可變（immutable）更安全
- ✅ 完全支援 Bean Validation
- ✅ 可以直接訪問欄位（不需要 `this.`）

### 4. 標準 Bean Validation 註解

| 註解 | 用途 | 範例 |
|------|------|------|
| `@NotNull` | 欄位不可為 null | `@NotNull Long id` |
| `@NotBlank` | 字串不可為空白 | `@NotBlank String name` |
| `@Size(min, max)` | 字串長度或集合大小 | `@Size(min=2, max=50)` |
| `@Min(value)` | 數字最小值 | `@Min(18) Integer age` |
| `@Max(value)` | 數字最大值 | `@Max(150) Integer age` |
| `@Email` | Email 格式驗證 | `@Email String email` |
| `@Pattern(regexp)` | 正規表達式驗證 | `@Pattern(regexp="^[A-Z].*")` |
| `@AssertTrue` | 自定義邏輯驗證 | 見上方範例 |
| `@Valid` | 觸發驗證 | `@Valid @RequestBody` |

### 5. 自定義驗證器

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

#### 步驟 2：實作驗證器（可注入 Repository）

```java
@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;  // null 由 @NotBlank 處理
        }
        return !userRepository.existsByEmail(email);
    }
}
```

### 6. 全域異常處理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Controller 層驗證失敗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(new ErrorResponse("驗證失敗", errors));
    }

    // Service 層驗證失敗
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(fieldName, violation.getMessage());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse("驗證失敗", errors));
    }
}
```

---

## API 端點

### 1. 使用者註冊（Controller 層驗證）

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

**成功回應（201 Created）：**
```json
{
  "id": 1,
  "name": "張三",
  "email": "zhangsan@example.com",
  "age": 25,
  "createdAt": "2025-11-27T18:00:00"
}
```

**驗證失敗回應（400 Bad Request）：**
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

### 2. 更新個人資料（Service 層驗證 ⭐）

展示如何驗證從 Service A 取得並傳入 Service B 的資料：

```http
PUT /api/users/1/profile?newPhone=0912345678
```

**成功回應（200 OK）：**
```
"成功更新使用者 測試用戶 的資料，新電話: 0912345678"
```

**工作流程：**
1. Controller 呼叫 `UserService.getUserData(1)` 取得用戶資料
2. 資料包裝成 `UserDataTransfer`（帶驗證註解）
3. 傳入 `ProfileService.updateProfile(userData, newPhone)`
4. ProfileService 的 `@Valid` 自動驗證所有欄位
5. 驗證失敗拋出 `ConstraintViolationException`

---

### 3. VIP 會員驗證（Record + @AssertTrue ⭐）

展示 Record 如何使用 `@AssertTrue` 進行複雜業務邏輯驗證：

```http
POST /api/users/vip/validate
Content-Type: application/json

{
  "userId": 1,
  "name": "張三",
  "age": 40,
  "vipLevel": 3,
  "discountRate": 25
}
```

**成功回應（200 OK）：**
```
"驗證通過！使用者: 張三, VIP等級: 3, 折扣率: 25%"
```

**驗證失敗範例（白金卡年齡不足）：**
```json
{
  "userId": 1,
  "name": "李四",
  "age": 25,
  "vipLevel": 3,
  "discountRate": 25
}
```

**失敗回應（400 Bad Request）：**
```json
{
  "message": "驗證失敗",
  "errors": {
    "validPlatinumAge": "白金卡僅限 30 歲以上會員申請"
  }
}
```

---

## 如何執行與測試

### 1. 啟動應用程式

**使用 IntelliJ IDEA（推薦）：**
1. 打開專案
2. 找到 `ValidationDemoApplication.java`
3. 右鍵 → `Run 'ValidationDemoApplication.main()'`
4. 等待看到 `Started ValidationDemoApplication` 訊息

**或使用命令列：**
```bash
# 確保在專案根目錄
cd D:\andy\POC\earlyReturnOptimize

# 編譯並執行
mvn clean spring-boot:run
```

### 2. 測試方式

#### 方式 A：使用 IntelliJ HTTP Client（推薦）

打開以下測試文件並執行：

1. **`api-tests.http`** - Controller 層驗證測試
   - 7 個測試案例
   - 涵蓋成功、失敗、多欄位驗證等

2. **`api-tests-profile.http`** - Service 層驗證測試
   - 展示跨 Service 的資料驗證

3. **`api-tests-record.http`** - Record + @AssertTrue 測試
   - 11 個測試案例
   - 展示複雜業務邏輯驗證

#### 方式 B：使用 Postman

詳細步驟請參考：
- **`POSTMAN_TEST_GUIDE.md`** - Controller 層測試指南
- **`RECORD_TEST_GUIDE.md`** - Record 測試指南

#### 方式 C：使用 curl

```bash
# 測試 1：成功註冊
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"張三","email":"test@example.com","age":25,"password":"password123"}'

# 測試 2：驗證失敗（年齡未滿 18）
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"王五","email":"wang@example.com","age":16,"password":"password123"}'

# 測試 3：Service 層驗證（先註冊用戶）
curl -X PUT "http://localhost:8080/api/users/1/profile?newPhone=0912345678"

# 測試 4：Record 驗證（VIP 會員）
curl -X POST http://localhost:8080/api/users/vip/validate \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"name":"張三","age":40,"vipLevel":3,"discountRate":25}'
```

### 3. 查看資料庫（H2 Console）

1. 訪問：http://localhost:8080/h2-console
2. 登入資訊：
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (留空)
3. 執行 SQL：
   ```sql
   SELECT * FROM users;
   ```

---

## 學習重點

### 1. 三種驗證層級

| 層級 | 觸發方式 | 拋出異常 | 適用場景 |
|-----|---------|---------|---------|
| **Controller 層** | `@Valid @RequestBody` | `MethodArgumentNotValidException` | 外部輸入驗證 |
| **Service 層** | `@Validated` + `@Valid` 參數 | `ConstraintViolationException` | 內部資料完整性驗證 |
| **DTO 內部邏輯** | `@AssertTrue` 方法 | 包含在上述異常中 | 跨欄位業務邏輯驗證 |

### 2. Record vs Class

| 特性 | Record | Class |
|-----|--------|-------|
| **語法** | 簡潔 | 需要 Lombok 或手動寫 |
| **可變性** | 不可變（immutable） | 可變（mutable） |
| **@AssertTrue 支援** | ✅ 完全支援 | ✅ 完全支援 |
| **適用場景** | DTO、Value Object | Entity、需要繼承 |
| **JPA 支援** | ❌ 不支援 | ✅ 支援 |

**推薦做法：**
- DTO（Request/Response）→ 使用 Record
- Entity（資料庫實體）→ 使用 Class
- Service 間傳遞 → 使用 Record

### 3. @AssertTrue 的使用時機

✅ **適合使用 @AssertTrue：**
- 跨欄位的業務邏輯驗證
- 需要同時檢查多個欄位的關聯性
- 複雜的條件判斷（如 VIP 等級與折扣率對應）

❌ **不適合使用 @AssertTrue：**
- 單一欄位的簡單驗證（用 `@NotNull`, `@Min` 等）
- 需要訪問外部服務或資料庫（用自定義 Validator）

### 4. 驗證執行順序

```
1. 基本約束驗證（@NotNull, @NotBlank, @Size, @Min, @Max, @Email）
   ↓
2. 如果基本驗證通過，執行 @AssertTrue 方法
   ↓
3. 如果是自定義 Validator，執行驗證邏輯
   ↓
4. 所有驗證都通過，才會執行業務邏輯
```

**重要：** 如果基本驗證失敗，`@AssertTrue` 不會執行。

---

## 延伸閱讀

### 📄 專案文件

1. **`SERVICE_LAYER_VALIDATION.md`** - Service 層驗證詳細指南
   - 介面 vs 實作類別的驗證配置
   - `@Validated` 和 `@Valid` 的正確用法
   - 完整的工作流程和範例

2. **`RECORD_VS_CLASS_VALIDATION.md`** - Record vs Class 完整對比
   - Record 的 Compact Constructor
   - 欄位訪問方式的差異
   - 何時使用 Record vs Class

3. **`DTO_VALIDATION_GUIDE.md`** - DTO 驗證與資料轉換
   - Entity → DTO vs 直接查詢 DTO
   - `@AssertTrue` 的詳細說明
   - 資料庫查詢的最佳實踐

4. **`POSTMAN_TEST_GUIDE.md`** - Postman 測試完整指南
5. **`RECORD_TEST_GUIDE.md`** - Record 測試指南

### 🔗 外部資源

- [Bean Validation 官方文檔](https://beanvalidation.org/)
- [Spring Boot Validation 指南](https://spring.io/guides/gs/validating-form-input/)
- [Hibernate Validator 文檔](https://hibernate.org/validator/)
- [Java Record 官方文檔](https://docs.oracle.com/en/java/javase/17/language/records.html)

---

## 總結

透過本專案，您可以學習到：

✅ **基礎技能：**
- Bean Validation 的基本使用
- 標準驗證註解的應用
- 全域異常處理的設計

✅ **進階技能：**
- Service 層的參數驗證（跨 Service 驗證）
- 自定義驗證器的實作（整合資料庫查詢）
- Record + @AssertTrue 的複雜業務邏輯驗證

✅ **最佳實踐：**
- 關注點分離（驗證 vs 業務邏輯）
- 聲明式編程（配置優於程式碼）
- 不可變資料結構（使用 Record）

**核心理念：** 讓驗證邏輯「看起來像配置，而不是程式碼」。

---

## 專案統計

- **程式碼減少：** 80%+ （相比傳統 Early Return）
- **API 端點：** 3 個（涵蓋三種驗證層級）
- **測試案例：** 20+ 個
- **文件：** 6 個詳細指南

---

**License:** MIT

**作者：** Claude Code

**最後更新：** 2025-11-27
