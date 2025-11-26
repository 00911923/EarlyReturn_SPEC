# Early Return 優化專案 - API 測試腳本
# 請確保應用已啟動在 http://localhost:8080

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Early Return 優化專案 - API 測試腳本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查應用是否啟動
Write-Host "檢查應用是否啟動..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method Get -ErrorAction Stop
    Write-Host "✓ 應用已啟動並運行中" -ForegroundColor Green
} catch {
    Write-Host "✗ 應用未啟動，請先啟動應用！" -ForegroundColor Red
    Write-Host "在 IntelliJ 中執行 ValidationDemoApplication" -ForegroundColor Yellow
    exit
}

Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 1: 成功註冊
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 1: 成功註冊使用者" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body1 = @{
    name = "張三"
    email = "zhangsan@example.com"
    age = 25
    password = "securePassword123"
} | ConvertTo-Json

Write-Host "請求資料:" -ForegroundColor Yellow
Write-Host $body1

$response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body1 -ContentType "application/json"
Write-Host ""
Write-Host "回應結果:" -ForegroundColor Green
$response1 | ConvertTo-Json
Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 2: Email 格式錯誤
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 2: Email 格式錯誤" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body2 = @{
    name = "李四"
    email = "invalid-email"
    age = 30
    password = "password123"
} | ConvertTo-Json

Write-Host "請求資料:" -ForegroundColor Yellow
Write-Host $body2

try {
    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body2 -ContentType "application/json"
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host ""
    Write-Host "驗證失敗 (預期行為):" -ForegroundColor Green
    $errorResponse | ConvertTo-Json
}
Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 3: 年齡未滿 18 歲
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 3: 年齡未滿 18 歲" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body3 = @{
    name = "王五"
    email = "wangwu@example.com"
    age = 16
    password = "password123"
} | ConvertTo-Json

Write-Host "請求資料:" -ForegroundColor Yellow
Write-Host $body3

try {
    $response3 = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body3 -ContentType "application/json"
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host ""
    Write-Host "驗證失敗 (預期行為):" -ForegroundColor Green
    $errorResponse | ConvertTo-Json
}
Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 4: 姓名太短
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 4: 姓名太短 (少於 2 個字元)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body4 = @{
    name = "王"
    email = "wang@example.com"
    age = 25
    password = "password123"
} | ConvertTo-Json

Write-Host "請求資料:" -ForegroundColor Yellow
Write-Host $body4

try {
    $response4 = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body4 -ContentType "application/json"
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host ""
    Write-Host "驗證失敗 (預期行為):" -ForegroundColor Green
    $errorResponse | ConvertTo-Json
}
Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 5: 密碼太短
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 5: 密碼太短 (少於 8 個字元)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body5 = @{
    name = "趙六"
    email = "zhaoliu@example.com"
    age = 25
    password = "123"
} | ConvertTo-Json

Write-Host "請求資料:" -ForegroundColor Yellow
Write-Host $body5

try {
    $response5 = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body5 -ContentType "application/json"
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host ""
    Write-Host "驗證失敗 (預期行為):" -ForegroundColor Green
    $errorResponse | ConvertTo-Json
}
Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 6: 重複的 Email (自定義驗證器)
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 6: 重複的 Email (自定義驗證器)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body6a = @{
    name = "孫七"
    email = "sunqi@example.com"
    age = 25
    password = "password123"
} | ConvertTo-Json

Write-Host "第一次註冊 (應該成功):" -ForegroundColor Yellow
Write-Host $body6a

$response6a = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body6a -ContentType "application/json"
Write-Host ""
Write-Host "回應結果:" -ForegroundColor Green
$response6a | ConvertTo-Json

Write-Host ""
Write-Host "第二次用相同 Email 註冊 (應該失敗):" -ForegroundColor Yellow

$body6b = @{
    name = "周八"
    email = "sunqi@example.com"
    age = 28
    password = "password456"
} | ConvertTo-Json

Write-Host $body6b

try {
    $response6b = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body6b -ContentType "application/json"
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host ""
    Write-Host "驗證失敗 (預期行為 - Email 已被註冊):" -ForegroundColor Green
    $errorResponse | ConvertTo-Json
}
Write-Host ""
Read-Host "按 Enter 繼續"

# 測試 7: 多個欄位同時驗證失敗
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試 7: 多個欄位同時驗證失敗" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$body7 = @{
    name = "吳"
    email = "invalid"
    age = 15
    password = "123"
} | ConvertTo-Json

Write-Host "請求資料 (所有欄位都有問題):" -ForegroundColor Yellow
Write-Host $body7

try {
    $response7 = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body7 -ContentType "application/json"
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host ""
    Write-Host "驗證失敗 (預期行為 - 多個欄位錯誤):" -ForegroundColor Green
    $errorResponse | ConvertTo-Json
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "所有測試完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "重點觀察:" -ForegroundColor Yellow
Write-Host "1. 驗證錯誤會返回結構化的 JSON，包含所有錯誤欄位" -ForegroundColor White
Write-Host "2. 自定義驗證器 @UniqueEmail 可以檢查資料庫中的資料" -ForegroundColor White
Write-Host "3. Service 層完全不需要寫驗證邏輯！" -ForegroundColor White
Write-Host ""
Read-Host "按 Enter 結束"
