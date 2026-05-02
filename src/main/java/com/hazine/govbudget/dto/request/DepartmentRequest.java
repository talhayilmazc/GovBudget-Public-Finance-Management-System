package com.hazine.govbudget.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequest {

    @NotBlank(message = "Departman adı boş olamaz")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Departman kodu boş olamaz")
    @Size(max = 20)
    private String code;

    @Size(max = 500)
    private String description;

    private Long parentId;
}