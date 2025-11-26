# API 測試指南

## 🚀 啟動應用

### 方法 1：使用 IntelliJ IDEA（推薦）

1. 打開 IntelliJ IDEA
2. 找到 `src/main/java/com/example/validation/ValidationDemoApplication.java`
3. 右鍵點擊 → `Run 'ValidationDemoApplication.main()'`
4. 等待應用啟動，看到類似訊息：
   ```
   Started ValidationDemoApplication in 2.5 seconds (process running for 3.1)
   ```

### 方法 2：使用命令列

```bash
cd D:\andy\POC\earlyReturnOptimize
mvn clean package
java -jar target/validation-demo-1.0.0.jar
```

---

## 🧪 測試方式

### 選項 1：使用測試腳本（最簡單）

**Windows**：
```bash
test-api.bat
```

這個腳本會自動執行所有測試案例。

### 選項 2：手動測試（使用 curl）

#### ✅ 測試 1：成功註冊

```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"張三\",\"email\":\"zhangsan@example.com\",\"age\":25,\"password\":\"securePassword123\"}"
```

**預期結果**：
```json
{
  "id": 1,
  "name": "張三",
  "email": "zhangsan@example.com",
  "age": 25,
  "createdAt": "2025-11-26T15:30:00"
}
```

---

#### ❌ 測試 2：Email 格式錯誤

```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"李四\",\"email\":\"invalid-email\",\"age\":30,\"password\":\"password123\"}"
```

**預期結果**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "email": "Email 格式不正確"
  }
}
```

---

#### ❌ 測試 3：年齡未滿 18 歲

```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"王五\",\"email\":\"wangwu@example.com\",\"age\":16,\"password\":\"password123\"}"
```

**預期結果**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "age": "年齡必須大於等於 18 歲"
  }
}
```

---

#### ❌ 測試 4：姓名太短

```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"王\",\"email\":\"wang@example.com\",\"age\":25,\"password\":\"password123\"}"
```

**預期結果**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "name": "姓名長度必須在 2-50 字元之間"
  }
}
```

---

#### ❌ 測試 5：密碼太短

```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"趙六\",\"email\":\"zhaoliu@example.com\",\"age\":25,\"password\":\"123\"}"
```

**預期結果**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "password": "密碼長度必須至少 8 個字元"
  }
}
```

---

#### ❌ 測試 6：重複的 Email（自定義驗證器）

**第一次註冊（成功）**：
```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"孫七\",\"email\":\"sunqi@example.com\",\"age\":25,\"password\":\"password123\"}"
```

**第二次用相同 Email（失敗）**：
```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"周八\",\"email\":\"sunqi@example.com\",\"age\":28,\"password\":\"password456\"}"
```

**預期結果**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "email": "Email 已被註冊"
  }
}
```

---

#### ❌ 測試 7：多個欄位同時驗證失敗

```bash
curl -X POST http://localhost:8080/api/users/register -H "Content-Type: application/json" -d "{\"name\":\"吳\",\"email\":\"invalid\",\"age\":15,\"password\":\"123\"}"
```

**預期結果**：
```json
{
  "message": "驗證失敗",
  "errors": {
    "name": "姓名長度必須在 2-50 字元之間",
    "email": "Email 格式不正確",
    "age": "年齡必須大於等於 18 歲",
    "password": "密碼長度必須至少 8 個字元"
  }
}
```

---

### 選項 3：使用 Postman

1. 打開 Postman
2. 建立新請求：
   - Method: `POST`
   - URL: `http://localhost:8080/api/users/register`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "name": "張三",
       "email": "zhangsan@example.com",
       "age": 25,
       "password": "securePassword123"
     }
     ```
3. 點擊 `Send`

---

## 🔍 查看資料庫（H2 Console）

1. 打開瀏覽器，訪問：http://localhost:8080/h2-console
2. 登入資訊：
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (留空)
3. 點擊 `Connect`
4. 執行 SQL 查詢：
   ```sql
   SELECT * FROM users;
   ```

---

## 📊 驗證重點

### Bean Validation 自動驗證

在 Controller 中只需要加上 `@Valid`：

```java
@PostMapping("/register")
public ResponseEntity<UserResponse> registerUser(
        @Valid @RequestBody UserRegistrationRequest request) {
    // 驗證已自動完成！
    UserResponse response = userService.registerUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### DTO 聲明式驗證

```java
public record UserRegistrationRequest(
    @NotBlank(message = "姓名不可為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在 2-50 字元之間")
    String name,

    @Email(message = "Email 格式不正確")
    @UniqueEmail(message = "Email 已被註冊")  // 自定義驗證器
    String email,

    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    Integer age
) {}
```

### 全域異常處理

所有驗證錯誤都會被 `GlobalExceptionHandler` 統一處理：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
    // 收集所有驗證錯誤並返回
}
```

---

## 🎯 學習重點對照

| 傳統 Early Return | Bean Validation |
|------------------|-----------------|
| ❌ 冗長的 if 判斷 | ✅ 聲明式註解 |
| ❌ 驗證邏輯分散 | ✅ 集中在 DTO |
| ❌ 難以維護 | ✅ 易於維護 |
| ❌ 手動寫錯誤訊息 | ✅ 自動處理 |
| ❌ Service 層混亂 | ✅ Service 層簡潔 |

---

## 💡 小技巧

### 快速重啟應用

每次修改程式碼後，按 `Ctrl + F5` (IntelliJ) 重新執行。

### 查看日誌

應用啟動時會顯示：
- SQL 語句（`show-sql: true`）
- 驗證錯誤訊息
- 請求處理流程

### 停止應用

- IntelliJ：點擊紅色停止按鈕
- 命令列：按 `Ctrl + C`

---

## ❓ 常見問題

### Q: 為什麼測試 6（重複 Email）在第一次執行時會失敗？

A: 因為使用 H2 記憶體資料庫，每次重啟應用資料會清空。確保在同一次執行中先註冊成功，再用相同 Email 註冊。

### Q: 如何測試所有案例？

A: 執行 `test-api.bat` 腳本，它會按順序測試所有情境。

### Q: 驗證失敗時返回什麼 HTTP 狀態碼？

A: 返回 `400 Bad Request`，這是 REST API 的標準做法。
