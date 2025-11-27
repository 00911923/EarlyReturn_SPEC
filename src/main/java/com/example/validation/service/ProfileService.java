package com.example.validation.service;

import com.example.validation.model.dto.request.UserDataTransfer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 個人資料服務介面
 */
public interface ProfileService {

    /**
     * 更新使用者個人資料
     *
     * 此方法會驗證傳入的 userData 是否完整
     * 即使 userData 來自另一個 Service，仍需確保所有必要欄位都有值
     *
     * 重點：@Valid 必須定義在介面上，而不是實作類別上
     *
     * @param userData 從其他 Service 取得的使用者資料（必須經過驗證）
     * @param newPhone 新的電話號碼
     * @return 更新後的訊息
     */
    String updateProfile(@Valid @NotNull UserDataTransfer userData, String newPhone);
}
