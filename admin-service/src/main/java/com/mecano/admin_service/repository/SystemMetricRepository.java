package com.mecano.admin_service.repository;

import com.mecano.admin_service.entity.SystemMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMetricRepository extends JpaRepository<SystemMetric, String> {

    List<SystemMetric> findByMetricNameOrderByCollectedAtDesc(String metricName);

    List<SystemMetric> findAllByOrderByCollectedAtDesc();
}