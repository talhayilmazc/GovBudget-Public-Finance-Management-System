package com.hazine.govbudget.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String BUDGET_EVENTS_TOPIC = "budget-events";
    public static final String EXPENSE_EVENTS_TOPIC = "expense-events";
    public static final String AUDIT_EVENTS_TOPIC = "audit-events";

    @Bean
    public NewTopic budgetEventsTopic() {
        return TopicBuilder.name(BUDGET_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic expenseEventsTopic() {
        return TopicBuilder.name(EXPENSE_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(AUDIT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}