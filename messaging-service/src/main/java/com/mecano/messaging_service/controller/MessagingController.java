package com.mecano.messaging_service.controller;

import com.mecano.messaging_service.config.RabbitMQConfig;
import com.mecano.messaging_service.dto.PaymentConfirmedEvent;
import com.mecano.messaging_service.dto.RepairRequestedEvent;
import com.mecano.messaging_service.dto.SubscriptionExpiredEvent;
import com.mecano.messaging_service.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messaging")
@RequiredArgsConstructor
public class MessagingController {

    private final RabbitTemplate rabbitTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Messaging Service UP ✅");
    }

    @PostMapping("/publish/user-registered")
    public ResponseEntity<String> publishUserRegistered(
            @RequestBody UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MECANO,
                RabbitMQConfig.KEY_USER_REGISTERED,
                event);
        return ResponseEntity.ok("Event publié → queue.user.registered");
    }

    @PostMapping("/publish/payment-confirmed")
    public ResponseEntity<String> publishPaymentConfirmed(
            @RequestBody PaymentConfirmedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MECANO,
                RabbitMQConfig.KEY_PAYMENT_CONFIRMED,
                event);
        return ResponseEntity.ok("Event publié → queue.payment.confirmed");
    }

    @PostMapping("/publish/repair-requested")
    public ResponseEntity<String> publishRepairRequested(
            @RequestBody RepairRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MECANO,
                RabbitMQConfig.KEY_REPAIR_REQUESTED,
                event);
        return ResponseEntity.ok("Event publié → queue.repair.requested");
    }

    @PostMapping("/publish/subscription-expired")
    public ResponseEntity<String> publishSubscriptionExpired(
            @RequestBody SubscriptionExpiredEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MECANO,
                RabbitMQConfig.KEY_SUBSCRIPTION_EXPIRED,
                event);
        return ResponseEntity.ok("Event publié → queue.subscription.expired");
    }
}