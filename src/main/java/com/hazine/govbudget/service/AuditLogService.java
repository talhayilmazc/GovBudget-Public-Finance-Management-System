package com.hazine.govbudget.service;

import com.hazine.govbudget.domain.enums.AuditAction;
import com.hazine.govbudget.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    void log(String entityName, Long entityId, AuditAction action,
             String oldValue, String newValue, String description);
    List<AuditLogResponse> getByEntity(String entityName, Long entityId);
    List<AuditLogResponse> getByUser(String username);
}