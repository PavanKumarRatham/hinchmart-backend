package com.example.hinchmart.repository;

import com.example.hinchmart.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByUserIdAndToken(Long userId, String token);
    Optional<DeviceToken> findByUserIdAndDeviceId(Long userId, String deviceId);
    List<DeviceToken> findByUserIdAndIsActiveTrue(Long userId);
    List<DeviceToken> findByUserIdAndUserRoleAndIsActiveTrue(Long userId, String userRole);
}
