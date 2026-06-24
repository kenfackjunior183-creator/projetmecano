package com.mecano.repair_service.repository;

import com.mecano.repair_service.entity.RepairRequest;
import com.mecano.repair_service.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, UUID> {
    List<RepairRequest> findByAutomobilistId(UUID automobilistId);
    List<RepairRequest> findByAssignedMechanicId(UUID mechanicId);
    List<RepairRequest> findByStatus(RequestStatus status);
}