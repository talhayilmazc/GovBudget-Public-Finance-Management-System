package com.hazine.govbudget.event;

import com.hazine.govbudget.domain.enums.BudgetStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetEvent {

    private Long budgetId;
    private String title;
    private BigDecimal totalAmount;
    private Integer fiscalYear;
    private BudgetStatus status;
    private String departmentCode;
    private String performedBy;
    private String eventType;
    private LocalDateTime occurredAt;
}