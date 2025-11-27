# Postman 測試指南

## 📋 前置準備

### 1. 啟動應用程式

**使用 IntelliJ IDEA（推薦）：**
1. 打開 IntelliJ IDEA
2. 找到 `src/main/java/com/example/validation/ValidationDemoApplication.java`
3. 右鍵 → `Run 'ValidationDemoApplication.main()'`
4. 等待應用啟動，看到訊息：
   ```
   Started ValidationDemoApplication in X.X seconds
   ```

### 2. 打開 Postman

確保 Postman 已安裝並開啟。

---

## 🧪 測試案例

### 測試 1：註冊新使用者（成功）

**目的：** 建立一個測試用戶，供後續測試使用

**設定 Postman：**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/users/register`
- **Headers:**
  - Key: `Content-Type`
  - Value: `application/json`
- **Body:** 選擇 `raw` 和 `JSON` 格式，輸入：
  ```json
  {
    "name": "測試用戶",
    "email": "test@example.com",
    "age": 25,
    "password": "password123"
  }
  ```

**預期回應：**
```json
{
  "id": 1,
  "name": "測試用戶",
  "email": "test@example.com",
  "age": 25,
  "createdAt": "2025-11-27T15:30:00"
}
```

**狀態碼：** `201 Created`

---

### 測試 2：Email 格式錯誤

**目的：** 驗證 Controller 層的 Bean Validation

**設定 Postman：**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/users/register`
- **Headers:**
  - Key: `Content-Type`
  - Value: `application/json`
- **Body:**
  ```json
  {
    "name": "李四",
    "email": "invalid-email",
    "age": 30,
    "password": "password123"
  }
  ```

**預期回應：**
```json
{
  "message": "驗證失敗",
  "errors": {
    "email": "Email 格式不正確"
  }
}
```

**狀態碼：** `400 Bad Request`

---

### 測試 3：多個欄位驗證失敗

**目的：** 驗證多個欄位同時失敗的情況

**設定 Postman：**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/users/register`
- **Headers:**
  - Key: `Content-Type`
  - Value: `application/json`
- **Body:**
  ```json
  {
    "name": "吳",
    "email": "invalid",
    "age": 15,
    "password": "123"
  }
  ```

**預期回應：**
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

**狀態碼：** `400 Bad Request`

---

## 🎯 Service 層驗證測試（新功能）

### 測試 4：更新個人資料（成功）

**目的：** 展示 Service 層的 @Valid 驗證
- Controller 從 UserService 取得資料
- 傳入 ProfileService 並自動驗證資料完整性

**設定 Postman：**
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/users/1/profile?newPhone=0912345678`
  - **注意：** `1` 是用戶 ID（使用測試 1 註冊的用戶）
  - **注意：** `newPhone` 是 Query Parameter
- **Headers:** 不需要特別設定
- **Body:** 不需要 Body

**預期回應：**
```json
"成功更新使用者 測試用戶 的資料，新電話: 0912345678"
```

**狀態碼：** `200 OK`

**背後發生的事情：**
1. Controller 呼叫 `UserService.getUserData(1)` 取得用戶資料
2. 資料包裝成 `UserDataTransfer`（帶有 @NotNull, @NotBlank 等驗證註解）
3. 傳入 `ProfileService.updateProfile(userData, "0912345678")`
4. ProfileService 的 `@Valid` 自動驗證 userData 的所有欄位
5. 驗證通過，執行業務邏輯

---

### 測試 5：使用不存在的用戶 ID

**目的：** 測試找不到用戶的情況

**設定 Postman：**
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/users/999/profile?newPhone=0912345678`
  - **注意：** `999` 是不存在的用戶 ID

**預期回應：**
```json
{
  "message": "系統發生錯誤，請稍後再試",
  "errors": {}
}
```

**狀態碼：** `500 Internal Server Error`

**說明：** UserService.getUserData() 找不到用戶時會拋出 RuntimeException

---

## 📸 Postman 操作截圖說明

### 如何設定 POST 請求（測試 1）

1. **選擇 Method：** 點擊左上角下拉選單，選擇 `POST`
2. **輸入 URL：** `http://localhost:8080/api/users/register`
3. **設定 Headers：**
   - 點擊 `Headers` 標籤
   - 新增一行：Key = `Content-Type`, Value = `application/json`
4. **設定 Body：**
   - 點擊 `Body` 標籤
   - 選擇 `raw`
   - 右側下拉選單選擇 `JSON`
   - 輸入 JSON 資料
5. **發送請求：** 點擊右上角藍色 `Send` 按鈕

### 如何設定 PUT 請求（測試 4）

1. **選擇 Method：** 選擇 `PUT`
2. **輸入 URL：** `http://localhost:8080/api/users/1/profile?newPhone=0912345678`
   - **方法 1：** 直接在 URL 欄位輸入完整 URL（包含 Query Parameter）
   - **方法 2：** 使用 Params 標籤：
     - URL 欄位輸入：`http://localhost:8080/api/users/1/profile`
     - 點擊 `Params` 標籤
     - 新增一行：Key = `newPhone`, Value = `0912345678`
3. **不需要 Body**
4. **發送請求：** 點擊 `Send`

---

## 🔍 如何查看 H2 資料庫

測試完成後，可以查看資料庫中儲存的資料：

1. 打開瀏覽器
2. 訪問：`http://localhost:8080/h2-console`
3. 登入資訊：
   - **JDBC URL:** `jdbc:h2:mem:testdb`
   - **User Name:** `sa`
   - **Password:** (留空)
4. 點擊 `Connect`
5. 執行 SQL：
   ```sql
   SELECT * FROM users;
   ```

---

## 📊 完整測試流程建議

**建議按照以下順序測試：**

1. ✅ **測試 1**：註冊成功（建立測試資料）
2. ✅ **測試 2**：Email 格式錯誤（驗證 Controller 層驗證）
3. ✅ **測試 3**：多欄位驗證失敗（驗證錯誤收集功能）
4. ✅ **測試 4**：更新個人資料成功（驗證 Service 層驗證）
5. ✅ **測試 5**：不存在的用戶 ID（驗證錯誤處理）

---

## 💡 Postman 小技巧

### 建立 Collection

1. 點擊左側 `Collections`
2. 點擊 `+ New Collection`
3. 命名為 `Bean Validation Demo`
4. 將所有測試請求加入此 Collection

### 使用 Environment Variables

如果要切換不同環境（本地、測試服務器等）：

1. 點擊右上角齒輪圖示
2. 建立 Environment，設定變數：
   - **Variable:** `baseUrl`
   - **Initial Value:** `http://localhost:8080`
3. URL 改用：`{{baseUrl}}/api/users/register`

### 儲存測試請求

每個測試完成後：
1. 點擊 `Save` 按鈕
2. 命名請求（如 "測試 1：註冊成功"）
3. 選擇 Collection
4. 下次可以直接從 Collection 中選擇並執行

---

## ❓ 常見問題

### Q1: 為什麼回應是 HTML 而不是 JSON？

**A:** URL 可能輸入錯誤，檢查：
- 確認 URL 是 `http://localhost:8080/api/users/register`
- 確認 Method 是 `POST`
- 確認 Header 有設定 `Content-Type: application/json`

### Q2: 回應 `404 Not Found`

**A:**
- 確認應用程式已啟動
- 確認 URL 正確（包含 `/api` 前綴）
- 確認 Method 正確（POST 或 PUT）

### Q3: 回應 `500 Internal Server Error`

**A:**
- 查看 IntelliJ 的 Console 視窗，檢查錯誤訊息
- 確認資料庫連線正常
- 確認 JSON 格式正確

### Q4: 測試 4 回應找不到用戶

**A:**
- 先執行測試 1 建立用戶
- 確認測試 1 回應的 `id` 欄位值
- 將測試 4 的 URL 中的 ID 改為正確的值

---

## 🎓 學習重點

透過 Postman 測試，你可以清楚看到：

1. **Controller 層驗證**（測試 2、3）：
   - 使用 `@Valid @RequestBody` 自動驗證
   - 拋出 `MethodArgumentNotValidException`
   - 返回結構化錯誤訊息

2. **Service 層驗證**（測試 4）：
   - 使用 `@Validated` + `@Valid` 參數驗證
   - 即使資料來自內部 Service，仍確保完整性
   - 拋出 `ConstraintViolationException`

3. **全域異常處理**：
   - 所有驗證錯誤統一格式
   - 友善的錯誤訊息
   - 一致的 HTTP 狀態碼

---

**祝測試順利！** 🚀
