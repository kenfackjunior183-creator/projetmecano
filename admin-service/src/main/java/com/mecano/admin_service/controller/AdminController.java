package com.mecano.admin_service.controller;

import com.mecano.admin_service.dto.AdminStatsResponse;
import com.mecano.admin_service.entity.AdminActionLog;
import com.mecano.admin_service.entity.SystemMetric;
import com.mecano.admin_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminStatsResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<AdminActionLog>> getActionLogs() {
        return ResponseEntity.ok(adminService.getActionLogs());
    }

    @GetMapping("/logs/me")
    public ResponseEntity<List<AdminActionLog>> getMyActionLogs(Authentication auth) {
        return ResponseEntity.ok(adminService.getActionLogsByAdmin(auth.getName()));
    }

    @PostMapping("/logs")
    public ResponseEntity<AdminActionLog> logAction(
            Authentication auth,
            @RequestParam String action,
            @RequestParam String description,
            @RequestParam String targetEntity,
            @RequestParam(required = false) String targetId) {
        return ResponseEntity.ok(
                adminService.logAction(auth.getName(), action, description, targetEntity, targetId));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<SystemMetric>> getMetrics() {
        return ResponseEntity.ok(adminService.getMetrics());
    }

    @GetMapping("/metrics/{metricName}")
    public ResponseEntity<List<SystemMetric>> getMetricsByName(@PathVariable String metricName) {
        return ResponseEntity.ok(adminService.getMetricsByName(metricName));
    }

    @PostMapping("/metrics")
    public ResponseEntity<SystemMetric> recordMetric(@RequestParam String metricName,
                                                      @RequestParam Long value) {
        return ResponseEntity.ok(adminService.recordMetric(metricName, value));
    }
}