package com.hazine.govbudget.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUpdateRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal totalAmount;

    private LocalDate startDate;
    private LocalDate endDate;
}