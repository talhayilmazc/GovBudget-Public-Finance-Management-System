package com.hazine.govbudget.service.impl;

import com.hazine.govbudget.domain.entity.AuditLog;
import com.hazine.govbudget.domain.enums.AuditAction;
import com.hazine.govbudget.domain.repository.AuditLogRepository;
import com.hazine.govbudget.dto.response.AuditLogResponse;
import com.hazine.govbudget.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Override
    @Transactional
    public void log(String entityName, Long entityId, AuditAction action,
                    String oldValue, String newValue, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .description(description)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} {} {}", entityName, entityId, action);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByEntity(String entityName, Long entityId) {
        return auditLogRepository
                .findByEntityNameAndEntityId(entityName, entityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByUser(String username) {
        return auditLogRepository
                .findByPerformedBy(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .performedBy(log.getCreatedBy())
                .ipAddress(log.getIpAddress())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
} 