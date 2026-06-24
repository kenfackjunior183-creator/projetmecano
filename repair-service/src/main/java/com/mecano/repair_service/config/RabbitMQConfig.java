package com.mecano.repair_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_MECANO = "mecano.exchange";
    public static final String QUEUE_REPAIR_REQUESTED = "queue.repair.requested";
    public static final String KEY_REPAIR_REQUESTED = "repair.requested";

    @Bean
    public TopicExchange mecanoExchange() {
        return new TopicExchange(EXCHANGE_MECANO);
    }

    @Bean
    public Queue queueRepairRequested() {
        return QueueBuilder.durable(QUEUE_REPAIR_REQUESTED).build();
    }

    @Bean
    public Binding bindingRepairRequested() {
        return BindingBuilder.bind(queueRepairRequested())
                .to(mecanoExchange()).with(KEY_REPAIR_REQUESTED);
    }

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