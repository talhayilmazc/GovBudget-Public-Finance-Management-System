package com.hazine.govbudget.dto.response;

import com.hazine.govbudget.domain.enums.AuditAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private String entityName;
    private Long entityId;
    private AuditAction action;
    private String oldValue;
    private String newValue;
    private String performedBy;
    private String ipAddress;
    private String description;
    private LocalDateTime createdAt;
}