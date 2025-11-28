# DTO 驗證與資料轉換指南

## 📊 資料流向：從資料庫到 DTO

### 方式 1：Entity → DTO（推薦用於複雜驗證）

```java
// Service 層
public UserDataTransfer getUserData(Long userId) {
    // 步驟 1: 從資料庫查詢 Entity
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("找不到使用者"));

    // 步驟 2: 轉換成 DTO
    return UserDataTransfer.fromUser(user);
}
```

**優點：**
- ✅ 可以在轉換時加入業務邏輯
- ✅ DTO 可以包含 `@AssertTrue` 等複雜驗證
- ✅ Entity 可以重複使用於不同 DTO
- ✅ 容易測試和維護

**缺點：**
- ❌ 需要額外的轉換步驟
- ❌ 會查詢所有欄位（即使不需要）

---

### 方式 2：直接查詢 DTO（適用於簡單查詢）

#### 2A. 介面投影（Interface Projection）

```java
// 定義投影介面
public interface UserDataProjection {
    Long getId();
    String getName();
    String getEmail();
    Integer getAge();
}

// Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<UserDataProjection> findProjectionById(Long id);
}

// Service
public UserDataTransfer getUserData(Long userId) {
    UserDataProjection projection = userRepository.findProjectionById(userId)
            .orElseThrow(() -> new RuntimeException("找不到使用者"));

    // 仍需轉換成 DTO
    return new UserDataTransfer(
        projection.getId(),
        projection.getName(),
        projection.getEmail(),
        projection.getAge()
    );
}
```

**優點：**
- ✅ 只查詢需要的欄位（效能較好）
- ✅ Spring Data JPA 自動實作

**缺點：**
- ❌ 仍需轉換成 DTO
- ❌ 無法直接使用 DTO 的驗證邏輯

---

#### 2B. 類別投影（Class-based Projection）

```java
// DTO 必須有對應的建構子
@Data
@NoArgsConstructor
@AllArgsConstructor  // ← 必須
public class UserDataTransfer {
    private Long userId;
    private String name;
    private String email;
    private Integer age;
}

// Repository - 使用 JPQL 的 new 關鍵字
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT new com.example.validation.model.dto.request.UserDataTransfer(" +
           "u.id, u.name, u.email, u.age) " +
           "FROM User u WHERE u.id = :id")
    Optional<UserDataTransfer> findUserDataById(@Param("id") Long id);
}

// Service - 直接返回 DTO
public UserDataTransfer getUserData(Long userId) {
    return userRepository.findUserDataById(userId)
            .orElseThrow(() -> new RuntimeException("找不到使用者"));
}
```

**優點：**
- ✅ 只查詢需要的欄位
- ✅ 直接返回 DTO，無需轉換

**缺點：**
- ❌ DTO 的 `@AssertTrue` 等驗證邏輯無法在查詢時執行
- ❌ 每個查詢需要寫獨立的 JPQL
- ❌ DTO 必須有對應的建構子

**⚠️ 重要限制：** 使用此方式時，DTO 中的 `@AssertTrue` 驗證方法無法使用，因為：
1. JPQL `new` 只會呼叫建構子
2. 驗證邏輯需要物件完全建立後才能執行

---

## 🔍 DTO 中使用 `@AssertTrue` 的完整範例

### 場景：VIP 會員系統

業務規則：
- 普通會員 (level 0): 無折扣
- 銀卡 (level 1): 5-10% 折扣
- 金卡 (level 2): 10-20% 折扣
- 白金卡 (level 3): 20-30% 折扣，且必須年滿 30 歲

### DTO 定義

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotNull(message = "使用者 ID 不可為空")
    private Long userId;

    @NotBlank(message = "姓名不可為空")
    private String name;

    @Email(message = "Email 格式不正確")
    private String email;

    @Min(value = 18, message = "年齡必須大於等於 18 歲")
    private Integer age;

    private Integer vipLevel;      // 0=普通, 1=銀卡, 2=金卡, 3=白金卡
    private Integer discountRate;  // 折扣率（百分比）

    /**
     * @AssertTrue 驗證方法
     *
     * 重點：
     * 1. 方法名必須以 is 或 has 開頭
     * 2. 返回值必須是 boolean
     * 3. 可以訪問物件的所有欄位
     * 4. 在所有基本驗證（@NotNull, @Min 等）通過後才執行
     */
    @AssertTrue(message = "VIP 等級與折扣率不符合規則")
    public boolean isValidVipDiscount() {
        if (vipLevel == null || discountRate == null) {
            return true;  // 允許為空（可選欄位）
        }

        return switch (vipLevel) {
            case 0 -> discountRate == 0;
            case 1 -> discountRate >= 5 && discountRate <= 10;
            case 2 -> discountRate >= 10 && discountRate <= 20;
            case 3 -> discountRate >= 20 && discountRate <= 30;
            default -> false;
        };
    }

    @AssertTrue(message = "白金卡僅限 30 歲以上會員申請")
    public boolean isValidPlatinumAge() {
        if (vipLevel == null || age == null) {
            return true;
        }

        if (vipLevel == 3) {
            return age >= 30;  // 白金卡必須年滿 30 歲
        }

        return true;
    }
}
```

---

## 🔄 從 Entity 轉換成包含 `@AssertTrue` 的 DTO

### Service 層實作

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VipService vipService;  // 假設有 VIP 服務

    public UserUpdateRequest getUserWithVipData(Long userId) {
        // 步驟 1: 從資料庫取得 User Entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // 步驟 2: 從其他服務取得 VIP 資料
        VipInfo vipInfo = vipService.getVipInfo(userId);

        // 步驟 3: 轉換成 DTO（包含所有必要資料）
        UserUpdateRequest dto = new UserUpdateRequest(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            vipInfo.getLevel(),      // VIP 等級
            vipInfo.getDiscountRate() // 折扣率
        );

        // 步驟 4: DTO 返回前，Spring Validation 會自動執行 @AssertTrue 驗證
        // 這發生在將 DTO 傳遞給其他 Service 時（如果該 Service 有 @Validated）

        return dto;
    }
}
```

---

## 📋 何時使用哪種方式？

### 使用 Entity → DTO（方式 1）

✅ **適用場景：**
- DTO 包含 `@AssertTrue` 等複雜驗證邏輯
- 需要組合多個 Entity 或服務的資料
- 需要在轉換時進行計算或邏輯處理
- 資料會被修改並寫回資料庫

**範例：**
```java
// 需要組合多個來源的資料
public UserProfileDTO getUserProfile(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    VipInfo vip = vipService.getVipInfo(userId);
    OrderStats stats = orderService.getUserStats(userId);

    // 組合成 DTO，並且 DTO 有 @AssertTrue 驗證
    return UserProfileDTO.from(user, vip, stats);
}
```

---

### 使用直接查詢 DTO（方式 2）

✅ **適用場景：**
- 只讀操作，不會修改資料
- 只需要部分欄位（效能優化）
- DTO 沒有複雜的驗證邏輯（只有 `@NotNull`, `@Size` 等簡單驗證）
- 查詢結果用於展示，不需要進一步處理

**範例：**
```java
// 只需要基本資料用於列表展示
@Query("SELECT new com.example.dto.UserListDTO(u.id, u.name, u.email) " +
       "FROM User u WHERE u.age > :minAge")
List<UserListDTO> findActiveUsers(@Param("minAge") int minAge);
```

---

## ⚠️ 重要注意事項

### 1. `@AssertTrue` 的限制

```java
// ❌ 錯誤：JPQL new 無法執行 @AssertTrue 驗證
@Query("SELECT new UserUpdateRequest(u.id, u.name, ...) FROM User u")
Optional<UserUpdateRequest> findUser(Long id);
// @AssertTrue 方法不會被呼叫！

// ✅ 正確：Entity → DTO，@AssertTrue 會在 Service 層驗證時執行
User user = userRepository.findById(id).orElseThrow();
UserUpdateRequest dto = UserUpdateRequest.fromEntity(user);
// 當這個 dto 傳遞給有 @Valid 的方法時，@AssertTrue 會執行
```

### 2. 驗證執行時機

```java
// Controller 層
@PostMapping("/update")
public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateRequest request) {
    // @AssertTrue 在這裡執行（Controller 層驗證）
    userService.updateUser(request);
    return ResponseEntity.ok("成功");
}

// Service 層
public interface UserService {
    void updateUser(@Valid UserUpdateRequest request);
}

@Service
@Validated
public class UserServiceImpl implements UserService {
    @Override
    public void updateUser(UserUpdateRequest request) {
        // @AssertTrue 在這裡也會執行（Service 層驗證）
    }
}
```

### 3. 驗證順序

Bean Validation 的驗證順序：

1. **基本約束驗證**：`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email` 等
2. **欄位級別的 `@AssertTrue`**：驗證單一欄位的邏輯
3. **類別級別的驗證**：跨欄位的 `@AssertTrue` 方法

如果任何一步失敗，後續驗證不會執行。

---

## 📝 完整範例總結

### 推薦做法（Entity → DTO）

```java
// 1️⃣ DTO 定義（包含複雜驗證）
@Data
public class UserUpdateRequest {
    @NotNull private Long userId;
    @NotBlank private String name;
    @Min(18) private Integer age;
    private Integer vipLevel;
    private Integer discountRate;

    @AssertTrue(message = "VIP 等級與折扣率不符")
    public boolean isValidVipDiscount() {
        // 複雜驗證邏輯
        return /* ... */;
    }
}

// 2️⃣ Repository（標準查詢 Entity）
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
}

// 3️⃣ Service（Entity → DTO 轉換）
@Service
public class UserServiceImpl {
    public UserUpdateRequest getUserData(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        VipInfo vip = vipService.getVipInfo(userId);

        // 組合並返回 DTO
        return UserUpdateRequest.fromEntity(user, vip);
    }
}

// 4️⃣ Controller 或其他 Service 使用（自動驗證）
@Service
@Validated
public class ProfileService {
    void updateProfile(@Valid UserUpdateRequest request) {
        // @AssertTrue 在這裡自動執行
    }
}
```

---

## 🎯 結論

**回答你的問題：**

1. **從資料庫取資料的方式：**
   - 通常是先取回 Entity，再轉換成 DTO
   - 也可以直接查詢 DTO，但會失去驗證邏輯

2. **DTO 包含 `@AssertTrue` 的寫法：**
   - ✅ 使用 Entity → DTO 方式
   - ✅ 在 DTO 中定義 `isXxx()` 方法
   - ✅ Service 層使用 `@Validated` + `@Valid` 觸發驗證
   - ❌ 不要用 JPQL `new` 直接查詢（驗證不會執行）

**最佳實踐：**
- 簡單查詢 → 直接查詢 DTO（效能優先）
- 複雜驗證 → Entity → DTO（靈活性優先）
