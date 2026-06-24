package com.mecano.admin_service.service;

import com.mecano.admin_service.dto.AdminStatsResponse;
import com.mecano.admin_service.entity.AdminActionLog;
import com.mecano.admin_service.entity.SystemMetric;
import com.mecano.admin_service.repository.AdminActionLogRepository;
import com.mecano.admin_service.repository.SystemMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminActionLogRepository actionLogRepository;
    private final SystemMetricRepository metricRepository;

    public AdminStatsResponse getDashboardStats() {
        return AdminStatsResponse.builder()
                .totalUsers(getMetricValue("total_users"))
                .totalRepairRequests(getMetricValue("total_repair_requests"))
                .totalListings(getMetricValue("total_listings"))
                .totalSubscriptions(getMetricValue("total_subscriptions"))
                .totalNotificationsSent(getMetricValue("total_notifications_sent"))
                .repairsByStatus(new HashMap<>())
                .usersByMonth(new HashMap<>())
                .build();
    }

    public AdminActionLog logAction(String adminEmail, String action, String description,
                                     String targetEntity, String targetId) {
        AdminActionLog log = AdminActionLog.builder()
                .adminEmail(adminEmail)
                .action(action)
                .description(description)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .build();
        return actionLogRepository.save(log);
    }

    public List<AdminActionLog> getActionLogs() {
        return actionLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AdminActionLog> getActionLogsByAdmin(String adminEmail) {
        return actionLogRepository.findByAdminEmailOrderByTimestampDesc(adminEmail);
    }

    public SystemMetric recordMetric(String metricName, Long value) {
        SystemMetric metric = SystemMetric.builder()
                .metricName(metricName)
                .metricValue(value)
                .build();
        return metricRepository.save(metric);
    }

    public List<SystemMetric> getMetrics() {
        return metricRepository.findAllByOrderByCollectedAtDesc();
    }

    public List<SystemMetric> getMetricsByName(String metricName) {
        return metricRepository.findByMetricNameOrderByCollectedAtDesc(metricName);
    }

    private long getMetricValue(String metricName) {
        List<SystemMetric> metrics = metricRepository.findByMetricNameOrderByCollectedAtDesc(metricName);
        return metrics.isEmpty() ? 0L : metrics.get(0).getMetricValue();
    }
}