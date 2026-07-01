package com.mecano.notification_service.listener;

import com.mecano.notification_service.config.RabbitMQConfig;
import com.mecano.notification_service.dto.*;
import com.mecano.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_REGISTERED)
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("📨 Event reçu : UserRegistered → {}", event.getEmail());
            notificationService.notifyUserRegistered(event);
            log.info("✅ Notification envoyée pour UserRegistered → {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Erreur traitement UserRegistered pour {} : {}", event.getEmail(), e.getMessage(), e);
            throw e; // Rejette le message pour retry / DLQ
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_CONFIRMED)
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        try {
            log.info("📨 Event reçu : PaymentConfirmed → {}", event.getEmail());
            notificationService.notifyPaymentConfirmed(event);
            log.info("✅ Notification envoyée pour PaymentConfirmed → {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Erreur traitement PaymentConfirmed pour {} : {}", event.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REPAIR_REQUESTED)
    public void onRepairRequested(RepairRequestedEvent event) {
        try {
            log.info("📨 Event reçu : RepairRequested → {} / {}", event.getAutomobilistEmail(), event.getMechanicEmail());
            notificationService.notifyRepairRequested(event);
            log.info("✅ Notifications envoyées pour RepairRequested");
        } catch (Exception e) {
            log.error("❌ Erreur traitement RepairRequested : {}", e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SUBSCRIPTION_EXPIRED)
    public void onSubscriptionExpired(SubscriptionExpiredEvent event) {
        try {
            log.info("📨 Event reçu : SubscriptionExpired → {}", event.getEmail());
            notificationService.notifySubscriptionExpired(event);
            log.info("✅ Notification envoyée pour SubscriptionExpired → {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Erreur traitement SubscriptionExpired pour {} : {}", event.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
