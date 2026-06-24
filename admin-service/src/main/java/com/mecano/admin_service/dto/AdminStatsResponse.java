package com.mecano.admin_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalRepairRequests;
    private long totalListings;
    private long totalSubscriptions;
    private long totalNotificationsSent;
    private Map<String, Long> repairsByStatus;
    private Map<String, Long> usersByMonth;
}