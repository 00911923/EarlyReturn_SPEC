# Service 層驗證指南

## 📖 使用場景

當 **Controller 從一個 Service 取得資料，然後傳入另一個 Service** 時，如何確保傳遞的資料完整性？

### 實際情境

```
Controller
  ↓
  1. 呼叫 UserService.getUserData(userId) 取得用戶資料
  ↓
  2. 取得 UserDataTransfer 物件
  ↓
  3. 呼叫 ProfileService.updateProfile(userData, newPhone)
  ↓
  4. ProfileService 需要驗證 userData 的所有欄位是否都有值
```

---

## 🎯 解決方案：Service 層使用 @Valid 驗證

### 步驟 1：建立帶有驗證註解的 DTO

**檔案：`UserDataTransfer.java`**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataTransfer {

    @NotNull(message = "使用者 ID 不可為空")
    private Long userId;

    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
    private String name;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotNull(message = "年齡不可為空")
    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    private Integer age;
}
```

### 步驟 2：Service 介面定義（⭐ 關鍵）

**檔案：`ProfileService.java`**

```java
public interface ProfileService {
    /**
     * 重點：@Valid 和 @NotNull 必須定義在介面上！
     * 根據 Bean Validation 規範，實作類別會自動繼承這些約束
     */
    String updateProfile(@Valid @NotNull UserDataTransfer userData, String newPhone);
}
```

### 步驟 3：Service 實作加上驗證

**檔案：`ProfileServiceImpl.java`**

```java
@Service
@Validated  // ← 重點 1：必須加上此註解才能啟用 Service 層驗證
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    @Override
    public String updateProfile(UserDataTransfer userData, String newPhone) {
        // ← 重點 2：方法參數不需要再加 @Valid（已在介面定義）

        // 此時 userData 已經通過驗證，確保所有必要欄位都有值
        log.info("更新使用者資料 - ID: {}, Name: {}, Email: {}, Age: {}",
                userData.getUserId(),
                userData.getName(),
                userData.getEmail(),
                userData.getAge());

        return String.format("成功更新使用者 %s 的資料", userData.getName());
    }
}
```

**關鍵要點：**

1. **`@Validated`**：加在 Service 實作類別上，啟用 Spring 的方法級別驗證
2. **`@Valid` 定義在介面上**：根據 Bean Validation 規範，實作類別會自動繼承驗證約束
3. **實作類別不可重複定義 `@Valid`**：否則會拋出 `ConstraintDeclarationException`
4. **驗證失敗會拋出 `ConstraintViolationException`**

### 步驟 4：Controller 呼叫流程

**檔案：`UserController.java`**

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;

    @PutMapping("/{userId}/profile")
    public ResponseEntity<String> updateProfile(
            @PathVariable Long userId,
            @RequestParam String newPhone) {

        // 步驟 1: 從 UserService 取得用戶資料
        UserDataTransfer userData = userService.getUserData(userId);

        // 步驟 2: 傳入 ProfileService
        // ProfileService 會自動驗證 userData 的所有欄位
        String result = profileService.updateProfile(userData, newPhone);

        return ResponseEntity.ok(result);
    }
}
```

### 步驟 5：全域異常處理

**檔案：`GlobalExceptionHandler.java`**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 處理 Service 層 Bean Validation 驗證失敗的異常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {

        log.warn("Service 層驗證失敗: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(fieldName, violation.getMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse("驗證失敗", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
```

---

## 🔄 完整流程圖

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Controller 接收請求                                           │
│    PUT /api/users/1/profile?newPhone=0912345678                 │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. Controller 呼叫 UserService.getUserData(1)                   │
│    ├─ 從資料庫查詢用戶資料                                        │
│    └─ 轉換為 UserDataTransfer                                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. Controller 呼叫 ProfileService.updateProfile(userData, ...)  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. ProfileService 方法執行前：Spring 自動驗證                     │
│    ├─ 檢查 userData.userId != null                              │
│    ├─ 檢查 userData.name 不為空且長度 2-50                        │
│    ├─ 檢查 userData.email 格式正確                               │
│    └─ 檢查 userData.age >= 18                                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                 ┌───────┴────────┐
                 │                │
          驗證成功                驗證失敗
                 │                │
                 ▼                ▼
    ┌────────────────┐  ┌─────────────────────────┐
    │ 執行業務邏輯    │  │ 拋出                     │
    │                │  │ ConstraintViolation     │
    │ 更新個人資料    │  │ Exception               │
    │                │  │                         │
    │ 返回成功訊息    │  │ → GlobalExceptionHandler│
    └────────────────┘  │ → 返回 400 + 錯誤訊息    │
                        └─────────────────────────┘
```

---

## 🧪 測試

### 測試 1：成功更新個人資料

**請求：**
```http
PUT http://localhost:8080/api/users/1/profile?newPhone=0912345678
```

**回應：**
```json
"成功更新使用者 測試用戶 的資料，新電話: 0912345678"
```

### 測試 2：驗證失敗（假設 UserDataTransfer 某欄位為 null）

如果從資料庫取得的資料有缺失（例如 name 為 null），驗證會自動失敗：

**回應：**
```json
{
  "message": "驗證失敗",
  "errors": {
    "name": "姓名不可為空"
  }
}
```

---

## 📊 兩種驗證層級對比

| 特性 | Controller 層驗證 | Service 層驗證 |
|-----|-----------------|--------------|
| **註解位置** | Controller 方法參數 | Service 方法參數 |
| **觸發時機** | HTTP 請求進入時 | Service 方法呼叫前 |
| **類別註解** | 不需要 | 需要 `@Validated` |
| **參數註解** | `@Valid @RequestBody` | `@Valid` |
| **拋出異常** | `MethodArgumentNotValidException` | `ConstraintViolationException` |
| **適用場景** | 外部輸入驗證 | 內部資料完整性驗證 |

---

## ⚙️ 關鍵配置要點

### 1. Service 類別必須加上 `@Validated`

```java
@Service
@Validated  // ← 沒有這個，@Valid 不會生效
public class ProfileServiceImpl implements ProfileService {
    // ...
}
```

### 2. `@Valid` 必須定義在介面上（⭐ 重要）

```java
// ✅ 正確：定義在介面上
public interface ProfileService {
    String updateProfile(@Valid @NotNull UserDataTransfer userData, String newPhone);
}

// ✅ 正確：實作類別不重複定義
@Service
@Validated
public class ProfileServiceImpl implements ProfileService {
    @Override
    public String updateProfile(UserDataTransfer userData, String newPhone) {
        // @Valid 已從介面繼承
    }
}

// ❌ 錯誤：實作類別重複定義會拋出異常
@Service
@Validated
public class ProfileServiceImpl implements ProfileService {
    @Override
    public String updateProfile(@Valid UserDataTransfer userData, String newPhone) {
        // ConstraintDeclarationException: 不可重複定義參數約束
    }
}
```

### 3. 全域異常處理器必須處理 `ConstraintViolationException`

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ErrorResponse> handleConstraintViolationException(
        ConstraintViolationException ex) {
    // 處理 Service 層驗證失敗
}
```

---

## ✅ 優點

1. **聲明式驗證**：不需要手動寫 `if (userData.getName() == null)` 這種程式碼
2. **可重用性**：`UserDataTransfer` 可在多個 Service 之間傳遞，驗證規則統一
3. **自動化**：Spring 自動在方法執行前進行驗證
4. **統一錯誤處理**：所有驗證錯誤都由 `GlobalExceptionHandler` 統一處理
5. **防禦性編程**：即使資料來自內部 Service，仍確保資料完整性

---

## ❌ 傳統做法 vs Bean Validation

### 傳統做法（Early Return）

```java
@Override
public String updateProfile(UserDataTransfer userData, String newPhone) {
    // 大量的手動驗證程式碼
    if (userData == null) {
        throw new IllegalArgumentException("userData 不可為空");
    }
    if (userData.getUserId() == null) {
        throw new IllegalArgumentException("使用者 ID 不可為空");
    }
    if (userData.getName() == null || userData.getName().isBlank()) {
        throw new IllegalArgumentException("姓名不可為空");
    }
    if (userData.getName().length() < 2 || userData.getName().length() > 50) {
        throw new IllegalArgumentException("姓名長度必須在 2-50 字元之間");
    }
    if (userData.getEmail() == null || userData.getEmail().isBlank()) {
        throw new IllegalArgumentException("Email 不可為空");
    }
    // ... 更多驗證 ...

    // 實際業務邏輯被淹沒在驗證程式碼中
    log.info("更新使用者資料...");
    return "成功";
}
```

### Bean Validation 做法

```java
@Service
@Validated
public class ProfileServiceImpl implements ProfileService {

    @Override
    public String updateProfile(@Valid UserDataTransfer userData, String newPhone) {
        // 驗證已自動完成，直接寫業務邏輯
        log.info("更新使用者資料...");
        return "成功";
    }
}
```

**程式碼減少 80%+，可讀性大幅提升！**

---

## 🎓 總結

當你需要在 Service 層驗證從另一個 Service 取得的資料時：

1. ✅ 建立帶有驗證註解的 DTO（如 `UserDataTransfer`）
2. ✅ Service 實作類別加上 `@Validated`
3. ✅ 方法參數加上 `@Valid`
4. ✅ 全域異常處理器處理 `ConstraintViolationException`

這樣就能優雅地確保跨 Service 傳遞的資料完整性，不需要手動寫大量的 `if` 判斷！
