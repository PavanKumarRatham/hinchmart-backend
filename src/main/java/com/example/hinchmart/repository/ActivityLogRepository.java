package com.example.hinchmart.repository;

import com.example.hinchmart.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserId(Long userId);
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<ActivityLog> findByAction(String action);
    List<ActivityLog> findByStatus(String status);
}
