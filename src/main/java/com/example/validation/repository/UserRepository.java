package com.example.validation.repository;

import com.example.validation.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 使用者 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 檢查 Email 是否已存在
     * @param email Email 地址
     * @return true 表示已存在
     */
    boolean existsByEmail(String email);
}
