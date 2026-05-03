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
public class ExpenseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExpenseEvent(ExpenseEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaConfig.EXPENSE_EVENTS_TOPIC,
                        String.valueOf(event.getExpenseId()),
                        event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Expense event published: {} offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish expense event: {}",
                        event.getEventType(), ex);
            }
        });
    }
}