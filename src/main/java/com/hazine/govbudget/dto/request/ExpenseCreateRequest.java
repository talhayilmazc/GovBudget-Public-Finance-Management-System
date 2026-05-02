package com.hazine.govbudget.dto.request;

import com.hazine.govbudget.domain.enums.ExpenseCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCreateRequest {

    @NotBlank(message = "Başlık boş olamaz")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Tutar boş olamaz")
    @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalıdır")
    private BigDecimal amount;

    @NotNull(message = "Harcama tarihi boş olamaz")
    private LocalDate expenseDate;

    @NotBlank(message = "Referans numarası boş olamaz")
    private String referenceNumber;

    @NotNull(message = "Kategori seçiniz")
    private ExpenseCategory category;

    @NotNull(message = "Bütçe seçiniz")
    private Long budgetId;

    private String invoiceNumber;
}