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
        log.info("📨 Event reçu : UserRegistered → {}", event.getEmail());
        notificationService.notifyUserRegistered(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_CONFIRMED)
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("📨 Event reçu : PaymentConfirmed → {}", event.getEmail());
        notificationService.notifyPaymentConfirmed(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REPAIR_REQUESTED)
    public void onRepairRequested(RepairRequestedEvent event) {
        log.info("📨 Event reçu : RepairRequested → {}", event.getAutomobilistEmail());
        notificationService.notifyRepairRequested(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SUBSCRIPTION_EXPIRED)
    public void onSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.info("📨 Event reçu : SubscriptionExpired → {}", event.getEmail());
        notificationService.notifySubscriptionExpired(event);
    }
}
