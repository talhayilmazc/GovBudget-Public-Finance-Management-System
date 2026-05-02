package com.hazine.govbudget.service.impl;

import com.hazine.govbudget.domain.entity.Department;
import com.hazine.govbudget.domain.enums.AuditAction;
import com.hazine.govbudget.domain.repository.DepartmentRepository;
import com.hazine.govbudget.dto.request.DepartmentRequest;
import com.hazine.govbudget.dto.response.DepartmentResponse;
import com.hazine.govbudget.exception.BusinessException;
import com.hazine.govbudget.exception.ResourceNotFoundException;
import com.hazine.govbudget.service.AuditLogService;
import com.hazine.govbudget.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                "Bu kod ile departman zaten mevcut: " + request.getCode(),
                "DEPARTMENT_CODE_EXISTS");
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        if (request.getParentId() != null) {
            Department parent = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Üst departman", request.getParentId()));
            department.setParent(parent);
        }

        Department saved = departmentRepository.save(department);
        auditLogService.log("Department", saved.getId(), AuditAction.CREATE,
                null, saved.getName(), "Departman oluşturuldu");
        log.info("Department created: {}", saved.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findById(id);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        Department saved = departmentRepository.save(department);
        auditLogService.log("Department", saved.getId(), AuditAction.UPDATE,
                null, saved.getName(), "Departman güncellendi");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "departments")
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAllActive()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getSubDepartments(Long parentId) {
        return departmentRepository.findByParentId(parentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public void delete(Long id) {
        Department department = findById(id);
        department.setDeleted(true);
        departmentRepository.save(department);
        auditLogService.log("Department", id, AuditAction.DELETE,
                department.getName(), null, "Departman silindi");
    }

    private Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departman", id));
    }

    private DepartmentResponse toResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .description(d.getDescription())
                .parentId(d.getParent() != null ? d.getParent().getId() : null)
                .parentName(d.getParent() != null ? d.getParent().getName() : null)
                .createdAt(d.getCreatedAt())
                .build();
    }
}