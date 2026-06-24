package com.mecano.admin_service.repository;

import com.mecano.admin_service.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, String> {

    List<AdminActionLog> findByAdminEmailOrderByTimestampDesc(String adminEmail);

    List<AdminActionLog> findByTargetEntityOrderByTimestampDesc(String targetEntity);

    List<AdminActionLog> findAllByOrderByTimestampDesc();
}