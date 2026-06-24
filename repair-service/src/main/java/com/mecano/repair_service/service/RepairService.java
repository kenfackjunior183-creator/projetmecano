package com.mecano.repair_service.service;

import com.mecano.repair_service.config.RabbitMQConfig;
import com.mecano.repair_service.dto.RepairRequestedEvent;
import com.mecano.repair_service.entity.RepairRequest;
import com.mecano.repair_service.entity.RequestStatus;
import com.mecano.repair_service.repository.RepairRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestRepository repairRepo;
    private final RabbitTemplate rabbitTemplate;

    // ── Création d'une demande de dépannage ───────────────────
    @Transactional
    public RepairRequest createRepairRequest(UUID automobilistId, Double lat, Double lng, String description) {
        return repairRepo.save(RepairRequest.builder()
                .automobilistId(automobilistId)
                .latitude(lat)
                .longitude(lng)
                .description(description)
                .status(RequestStatus.PENDING)
                .build());
    }

    // ── Assignation d'un mécanicien ───────────────────────────
    @Transactional
    public RepairRequest assignMechanic(UUID repairRequestId, UUID mechanicId) {
        RepairRequest req = repairRepo.findById(repairRequestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        req.setAssignedMechanicId(mechanicId);
        req.setStatus(RequestStatus.ACCEPTED);
        return repairRepo.save(req);
    }

    // ── Mise à jour du statut ─────────────────────────────────
    @Transactional
    public RepairRequest updateStatus(UUID repairRequestId, RequestStatus status) {
        RepairRequest req = repairRepo.findById(repairRequestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        req.setStatus(status);
        return repairRepo.save(req);
    }

    // ── Récupération des demandes ─────────────────────────────
    public List<RepairRequest> getByAutomobilist(UUID automobilistId) {
        return repairRepo.findByAutomobilistId(automobilistId);
    }

    public List<RepairRequest> getByMechanic(UUID mechanicId) {
        return repairRepo.findByAssignedMechanicId(mechanicId);
    }

    public List<RepairRequest> getByStatus(RequestStatus status) {
        return repairRepo.findByStatus(status);
    }

    // ── Publication d'événement de demande de dépannage ───────
    public void publishRepairRequestedEvent(RepairRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MECANO,
                RabbitMQConfig.KEY_REPAIR_REQUESTED,
                event);
    }
}