package com.hazine.govbudget.event;

import com.hazine.govbudget.domain.enums.ExpenseCategory;
import com.hazine.govbudget.domain.enums.ExpenseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseEvent {

    private Long expenseId;
    private String title;
    private BigDecimal amount;
    private ExpenseCategory category;
    private ExpenseStatus status;
    private Long budgetId;
    private String submittedBy;
    private String approvedBy;
    private String eventType;
    private LocalDateTime occurredAt;
}