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
public class BudgetCreateRequest {

    @NotBlank(message = "Başlık boş olamaz")
    @Size(max = 200, message = "Başlık 200 karakteri aşamaz")
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Tutar boş olamaz")
    @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalıdır")
    private BigDecimal totalAmount;

    @NotNull(message = "Mali yıl boş olamaz")
    @Min(value = 2000, message = "Geçerli bir mali yıl giriniz")
    private Integer fiscalYear;

    @NotNull(message = "Başlangıç tarihi boş olamaz")
    private LocalDate startDate;

    @NotNull(message = "Bitiş tarihi boş olamaz")
    private LocalDate endDate;

    @NotNull(message = "Departman seçiniz")
    private Long departmentId;
}