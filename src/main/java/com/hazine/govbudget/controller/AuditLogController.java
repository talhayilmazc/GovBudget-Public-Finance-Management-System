package com.hazine.govbudget.controller;

import com.hazine.govbudget.dto.response.AuditLogResponse;
import com.hazine.govbudget.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Denetim kayıtları")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @Operation(summary = "Varlığa göre denetim kayıtları")
    public ResponseEntity<List<AuditLogResponse>> getByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(
                auditLogService.getByEntity(entityName, entityId));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @Operation(summary = "Kullanıcıya göre denetim kayıtları")
    public ResponseEntity<List<AuditLogResponse>> getByUser(
            @PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getByUser(username));
    }
}