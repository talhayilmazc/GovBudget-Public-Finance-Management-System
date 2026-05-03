package com.hazine.govbudget.event;

import com.hazine.govbudget.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BudgetEventConsumer {

    @KafkaListener(
            topics = KafkaConfig.BUDGET_EVENTS_TOPIC,
            groupId = "govbudget-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBudgetEvent(BudgetEvent event) {
        log.info("Budget event consumed: type={} budgetId={} status={}",
                event.getEventType(),
                event.getBudgetId(),
                event.getStatus());
        // Bildirim, raporlama veya başka servisler buraya eklenebilir
    }
}