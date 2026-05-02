package com.hazine.govbudget.domain.repository;

import com.hazine.govbudget.domain.entity.AuditLog;
import com.hazine.govbudget.domain.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
        JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    List<AuditLog> findByPerformedBy(String username);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByEntityName(String entityName);
}