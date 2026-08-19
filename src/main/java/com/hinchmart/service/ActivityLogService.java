package com.hinchmart.service;

import com.hinchmart.entity.ActivityLog;
import com.hinchmart.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(Long userId, String userEmail, String action, String entityType, Long entityId, String details, String ipAddress) {
        try {
            ActivityLog log = new ActivityLog(userId, userEmail, action, entityType, entityId, details, ipAddress);
            activityLogRepository.save(log);
        } catch (Exception ex) {
            logger.error("Failed to write activity log: {}", ex.getMessage());
        }
    }
}
