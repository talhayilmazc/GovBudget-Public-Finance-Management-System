package com.hazine.govbudget.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdAt;
}