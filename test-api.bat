@echo off
chcp 65001 >nul
echo ========================================
echo Early Return 優化專案 - API 測試腳本
echo ========================================
echo.

echo 請確認應用已啟動在 http://localhost:8080
echo.
pause

echo.
echo ========================================
echo 測試 1: 成功註冊使用者
echo ========================================
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"張三\",\"email\":\"zhangsan@example.com\",\"age\":25,\"password\":\"securePassword123\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 測試 2: Email 格式錯誤
echo ========================================
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"李四\",\"email\":\"invalid-email\",\"age\":30,\"password\":\"password123\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 測試 3: 年齡未滿 18 歲
echo ========================================
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"王五\",\"email\":\"wangwu@example.com\",\"age\":16,\"password\":\"password123\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 測試 4: 姓名太短（少於 2 個字元）
echo ========================================
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"王\",\"email\":\"wang@example.com\",\"age\":25,\"password\":\"password123\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 測試 5: 密碼太短（少於 8 個字元）
echo ========================================
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"趙六\",\"email\":\"zhaoliu@example.com\",\"age\":25,\"password\":\"123\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 測試 6: 重複的 Email（先成功註冊一個）
echo ========================================
echo 第一次註冊（應該成功）:
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"孫七\",\"email\":\"sunqi@example.com\",\"age\":25,\"password\":\"password123\"}"
echo.
echo.
echo 第二次用相同 Email 註冊（應該失敗）:
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"周八\",\"email\":\"sunqi@example.com\",\"age\":28,\"password\":\"password456\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 測試 7: 多個欄位同時驗證失敗
echo ========================================
curl -X POST http://localhost:8080/api/users/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"吳\",\"email\":\"invalid\",\"age\":15,\"password\":\"123\"}"
echo.
echo.
pause

echo.
echo ========================================
echo 所有測試完成！
echo ========================================
pause
