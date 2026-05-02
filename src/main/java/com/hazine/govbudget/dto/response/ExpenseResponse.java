package com.hazine.govbudget.dto.response;

import com.hazine.govbudget.domain.enums.ExpenseCategory;
import com.hazine.govbudget.domain.enums.ExpenseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String referenceNumber;
    private ExpenseCategory category;
    private ExpenseStatus status;
    private String budgetTitle;
    private Long budgetId;
    private String submittedByUsername;
    private String approvedByUsername;
    private String rejectionReason;
    private String invoiceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}