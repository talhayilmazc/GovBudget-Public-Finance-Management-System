package com.hazine.govbudget.event;

import com.hazine.govbudget.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBudgetEvent(BudgetEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaConfig.BUDGET_EVENTS_TOPIC,
                        String.valueOf(event.getBudgetId()),
                        event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Budget event published: {} offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish budget event: {}",
                        event.getEventType(), ex);
            }
        });
    }
}