package com.mecano.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Noms des exchanges ──────────────────────────────────────
    public static final String EXCHANGE_MECANO = "mecano.exchange";

    // ── Noms des queues ─────────────────────────────────────────
    public static final String QUEUE_USER_REGISTERED    = "queue.user.registered";
    public static final String QUEUE_PAYMENT_CONFIRMED  = "queue.payment.confirmed";
    public static final String QUEUE_REPAIR_REQUESTED   = "queue.repair.requested";
    public static final String QUEUE_SUBSCRIPTION_EXPIRED = "queue.subscription.expired";

    // ── Dead Letter Queue ───────────────────────────────────────
    public static final String DLX_MECANO = "mecano.dlx";
    public static final String QUEUE_DLQ = "queue.dlq";

    // ── Routing keys ────────────────────────────────────────────
    public static final String KEY_USER_REGISTERED      = "user.registered";
    public static final String KEY_PAYMENT_CONFIRMED    = "payment.confirmed";
    public static final String KEY_REPAIR_REQUESTED     = "repair.requested";
    public static final String KEY_SUBSCRIPTION_EXPIRED = "subscription.expired";

    // ── Exchange ────────────────────────────────────────────────
    @Bean
    public TopicExchange mecanoExchange() {
        return new TopicExchange(EXCHANGE_MECANO);
    }

    // ── Queues ──────────────────────────────────────────────────
    @Bean public Queue queueUserRegistered() {
        return QueueBuilder.durable(QUEUE_USER_REGISTERED)
                .withArgument("x-dead-letter-exchange", DLX_MECANO)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    @Bean public Queue queuePaymentConfirmed() {
        return QueueBuilder.durable(QUEUE_PAYMENT_CONFIRMED)
                .withArgument("x-dead-letter-exchange", DLX_MECANO)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    @Bean public Queue queueRepairRequested() {
        return QueueBuilder.durable(QUEUE_REPAIR_REQUESTED)
                .withArgument("x-dead-letter-exchange", DLX_MECANO)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    @Bean public Queue queueSubscriptionExpired() {
        return QueueBuilder.durable(QUEUE_SUBSCRIPTION_EXPIRED)
                .withArgument("x-dead-letter-exchange", DLX_MECANO)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    // ── Dead Letter Exchange & Queue ────────────────────────────
    @Bean public FanoutExchange dlxExchange() {
        return new FanoutExchange(DLX_MECANO);
    }
    @Bean public Queue queueDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }
    @Bean public Binding bindingDlq() {
        return BindingBuilder.bind(queueDlq()).to(dlxExchange());
    }

    // ── Bindings ─────────────────────────────────────────────────
    @Bean public Binding bindingUserRegistered() {
        return BindingBuilder.bind(queueUserRegistered())
                .to(mecanoExchange()).with(KEY_USER_REGISTERED);
    }
    @Bean public Binding bindingPaymentConfirmed() {
        return BindingBuilder.bind(queuePaymentConfirmed())
                .to(mecanoExchange()).with(KEY_PAYMENT_CONFIRMED);
    }
    @Bean public Binding bindingRepairRequested() {
        return BindingBuilder.bind(queueRepairRequested())
                .to(mecanoExchange()).with(KEY_REPAIR_REQUESTED);
    }
    @Bean public Binding bindingSubscriptionExpired() {
        return BindingBuilder.bind(queueSubscriptionExpired())
                .to(mecanoExchange()).with(KEY_SUBSCRIPTION_EXPIRED);
    }

    // ── Convertisseur JSON ──────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
