package com.hazine.govbudget.dto.response;

import com.hazine.govbudget.domain.enums.BudgetStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal totalAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Integer fiscalYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetStatus status;
    private String departmentName;
    private String departmentCode;
    private String createdByUsername;
    private String approvedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}