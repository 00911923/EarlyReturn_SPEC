# Record vs Class：使用 @AssertTrue 驗證

## ✅ 答案：Record 可以使用 @AssertTrue！

Record（Java 14+）完全支援 Bean Validation，包括 `@AssertTrue`。

---

## 📊 Record vs Class 對比

### 方式 1：使用 Class（傳統做法）

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVipRequest {

    @NotNull(message = "使用者 ID 不可為空")
    private Long userId;

    @NotBlank(message = "姓名不可為空")
    private String name;

    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    private Integer age;

    private Integer vipLevel;
    private Integer discountRate;

    // 需要使用 this.vipLevel（Lombok @Data 會生成 getter）
    @AssertTrue(message = "VIP 等級與折扣率不符")
    public boolean isValidVipDiscount() {
        if (this.vipLevel == null || this.discountRate == null) {
            return true;
        }

        return switch (this.vipLevel) {
            case 0 -> this.discountRate == 0;
            case 1 -> this.discountRate >= 5 && this.discountRate <= 10;
            case 2 -> this.discountRate >= 10 && this.discountRate <= 20;
            case 3 -> this.discountRate >= 20 && this.discountRate <= 30;
            default -> false;
        };
    }
}
```

**特點：**
- ✅ 可變（mutable）- 可以用 setter 修改
- ✅ 需要 Lombok 或手動寫 getter/setter/constructor
- ✅ 程式碼較冗長
- ⚠️ 需要使用 `this.` 或 getter 方法訪問欄位

---

### 方式 2：使用 Record（推薦，Java 17+）

```java
public record UserVipRequest(

        @NotNull(message = "使用者 ID 不可為空")
        Long userId,

        @NotBlank(message = "姓名不可為空")
        String name,

        @Min(value = 18, message = "年齡必須大於等於 18 歲")
        Integer age,

        Integer vipLevel,
        Integer discountRate

) {
    // 可以直接訪問欄位（不需要 this.）
    @AssertTrue(message = "VIP 等級與折扣率不符")
    public boolean isValidVipDiscount() {
        if (vipLevel == null || discountRate == null) {
            return true;
        }

        return switch (vipLevel) {
            case 0 -> discountRate == 0;
            case 1 -> discountRate >= 5 && discountRate <= 10;
            case 2 -> discountRate >= 10 && discountRate <= 20;
            case 3 -> discountRate >= 20 && discountRate <= 30;
            default -> false;
        };
    }

    @AssertTrue(message = "白金卡僅限 30 歲以上會員")
    public boolean isValidPlatinumAge() {
        if (vipLevel == null || age == null) {
            return true;
        }

        if (vipLevel == 3) {
            return age >= 30;
        }

        return true;
    }
}
```

**特點：**
- ✅ 不可變（immutable）- 更安全
- ✅ 語法簡潔，自動生成 constructor/getter/equals/hashCode/toString
- ✅ 不需要 Lombok
- ✅ 可以直接訪問欄位（語法更清晰）
- ✅ 完全支援 Bean Validation

---

## 🔑 Record 使用 @AssertTrue 的關鍵要點

### 1. 方法命名規則（與 Class 相同）

```java
public record UserVipRequest(...) {

    // ✅ 正確：以 is 開頭
    @AssertTrue(message = "...")
    public boolean isValidVipDiscount() {
        return /* ... */;
    }

    // ✅ 正確：以 has 開頭
    @AssertTrue(message = "...")
    public boolean hasValidAge() {
        return /* ... */;
    }

    // ❌ 錯誤：方法名不符合規則
    @AssertTrue(message = "...")
    public boolean checkVipDiscount() {
        return /* ... */;
    }
}
```

### 2. 欄位訪問方式

```java
public record UserVipRequest(Integer vipLevel, Integer discountRate) {

    @AssertTrue(message = "...")
    public boolean isValidVipDiscount() {
        // ✅ Record 推薦：直接使用欄位名
        if (vipLevel == null) {
            return true;
        }

        // ✅ 也可以使用 accessor 方法（Record 的 getter 就是欄位名）
        if (vipLevel() == null) {
            return true;
        }

        // ⚠️ Class 的方式（也能用，但不推薦）
        if (this.vipLevel == null) {
            return true;
        }

        return true;
    }
}
```

**注意：** Record 的 getter 方法名稱是**欄位名本身**，不是 `getXxx()`！

```java
// Record
public record User(Long id, String name) {}

// 使用方式
User user = new User(1L, "張三");
Long id = user.id();        // ✅ Record 的 getter
String name = user.name();  // ✅ Record 的 getter

// Class（Lombok @Data）
@Data
public class User {
    private Long id;
    private String name;
}

// 使用方式
User user = new User();
Long id = user.getId();      // ✅ Class 的 getter
String name = user.getName(); // ✅ Class 的 getter
```

### 3. Compact Constructor（Record 特有功能）

Record 可以使用 Compact Constructor 在建構時進行預處理：

```java
public record UserVipRequest(
        Long userId,
        String name,
        Integer age,
        Integer vipLevel,
        Integer discountRate
) {
    /**
     * Compact Constructor（緊湊建構子）
     *
     * 在物件建立時執行，可以：
     * 1. 驗證參數
     * 2. 標準化資料
     * 3. 設定預設值
     *
     * 注意：這裡的驗證不是 Bean Validation，是在建構時執行的
     */
    public UserVipRequest {
        // 參數標準化
        if (name != null) {
            name = name.trim();  // 去除空白
        }

        // 設定預設值
        if (vipLevel == null) {
            vipLevel = 0;  // 預設為普通會員
        }
        if (discountRate == null) {
            discountRate = 0;  // 預設無折扣
        }

        // 建構時驗證（拋出異常）
        if (vipLevel < 0 || vipLevel > 3) {
            throw new IllegalArgumentException("VIP 等級必須在 0-3 之間");
        }
    }

    // Bean Validation（在 @Valid 觸發時執行）
    @AssertTrue(message = "VIP 等級與折扣率不符")
    public boolean isValidVipDiscount() {
        return switch (vipLevel) {
            case 0 -> discountRate == 0;
            case 1 -> discountRate >= 5 && discountRate <= 10;
            case 2 -> discountRate >= 10 && discountRate <= 20;
            case 3 -> discountRate >= 20 && discountRate <= 30;
            default -> false;
        };
    }
}
```

**Compact Constructor vs @AssertTrue 的區別：**

| 特性 | Compact Constructor | @AssertTrue |
|-----|-------------------|------------|
| **執行時機** | 物件建立時（new） | Bean Validation 觸發時（@Valid） |
| **失敗行為** | 拋出異常（IllegalArgumentException） | 拋出 ConstraintViolationException |
| **用途** | 資料標準化、基本檢查 | 業務邏輯驗證 |
| **適用場景** | 建構時必須滿足的條件 | 外部輸入驗證、跨欄位驗證 |

### 4. 工廠方法

Record 可以定義靜態工廠方法：

```java
public record UserVipRequest(...) {

    /**
     * 從 Entity 創建的工廠方法
     */
    public static UserVipRequest fromEntity(User user, Integer vipLevel, Integer discountRate) {
        return new UserVipRequest(
            user.getId(),
            user.getName(),
            user.getAge(),
            vipLevel,
            discountRate
        );
    }

    /**
     * 從多個來源組合資料的工廠方法
     */
    public static UserVipRequest from(User user, VipInfo vipInfo) {
        return new UserVipRequest(
            user.getId(),
            user.getName(),
            user.getAge(),
            vipInfo.getLevel(),
            vipInfo.getDiscountRate()
        );
    }
}
```

---

## 📋 完整使用範例

### Service 層

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VipService vipService;

    /**
     * 使用 Record 作為返回值
     */
    public UserVipRequest getUserVipData(Long userId) {
        // 從資料庫取得 User Entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 從其他服務取得 VIP 資料
        VipInfo vipInfo = vipService.getVipInfo(userId);

        // 使用工廠方法創建 Record
        return UserVipRequest.from(user, vipInfo);
    }
}
```

### Controller 層

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 接收 Record 作為請求參數
     * @Valid 會自動觸發 @AssertTrue 驗證
     */
    @PostMapping("/vip/update")
    public ResponseEntity<String> updateVip(@Valid @RequestBody UserVipRequest request) {
        // @AssertTrue 在這裡自動執行
        // 如果驗證失敗，拋出 MethodArgumentNotValidException
        userService.updateVip(request);
        return ResponseEntity.ok("更新成功");
    }
}
```

### Service 層驗證

```java
// Service 介面
public interface VipService {
    void updateVip(@Valid @NotNull UserVipRequest request);
}

// Service 實作
@Service
@Validated  // 必須加這個
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    @Override
    public void updateVip(UserVipRequest request) {
        // @AssertTrue 在這裡也會執行（Service 層驗證）
        // 驗證失敗拋出 ConstraintViolationException

        // 業務邏輯...
    }
}
```

---

## ⚖️ Record vs Class：何時使用？

### 使用 Record 的場景 ✅

1. **DTO（資料傳輸物件）**
   - 請求/回應 DTO
   - Service 間傳遞的資料物件

2. **不可變資料**
   - 配置物件
   - 查詢結果

3. **值物件（Value Object）**
   - DDD 中的 Value Object
   - 不需要修改的資料

**範例：**
```java
// ✅ Record 適合
public record UserRegistrationRequest(...) {}
public record OrderDTO(...) {}
public record VipInfo(...) {}
```

### 使用 Class 的場景 ✅

1. **Entity（JPA 實體）**
   - JPA 需要 no-arg constructor
   - 需要延遲加載（Lazy Loading）

2. **需要繼承的情況**
   - Record 不能被繼承（final）
   - Record 不能繼承其他類別（除了 Object）

3. **需要可變狀態**
   - 需要 setter 方法修改欄位

**範例：**
```java
// ✅ Class 適合
@Entity
public class User { ... }  // JPA Entity

// ❌ Record 不適合
// public record User(...) {}  // Record 不能作為 JPA Entity
```

---

## 🎯 最佳實踐建議

### 1. DTO 優先使用 Record

```java
// ✅ 推薦：Request/Response DTO 使用 Record
public record UserRegistrationRequest(
    @NotBlank String name,
    @Email String email,
    @Min(18) Integer age
) {
    @AssertTrue(message = "...")
    public boolean isValidAge() {
        return age >= 18 && age <= 150;
    }
}
```

### 2. Entity 使用 Class

```java
// ✅ 推薦：JPA Entity 使用 Class
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private Integer age;
}
```

### 3. Service 間傳遞使用 Record

```java
// ✅ 推薦：Service 間傳遞的資料物件使用 Record
public record UserDataTransfer(
    @NotNull Long userId,
    @NotBlank String name,
    @Email String email,
    @Min(18) Integer age
) {
    @AssertTrue(message = "所有必要欄位都必須有值")
    public boolean hasAllRequiredFields() {
        return userId != null && name != null && email != null && age != null;
    }
}
```

---

## 📝 總結

**回答你的問題：**

> `@AssertTrue` 可不可以配 Record 去做？

✅ **可以！而且推薦使用！**

**優點：**
1. ✅ 語法更簡潔
2. ✅ 不可變性更安全
3. ✅ 不需要 Lombok
4. ✅ 完全支援 Bean Validation
5. ✅ 欄位訪問更直觀（不需要 `this.`）

**唯一限制：**
- Record 不能作為 JPA Entity（Entity 必須用 Class）

**推薦做法：**
- DTO（Request/Response）→ 使用 Record
- Entity（資料庫實體）→ 使用 Class
- Service 間傳遞 → 使用 Record
