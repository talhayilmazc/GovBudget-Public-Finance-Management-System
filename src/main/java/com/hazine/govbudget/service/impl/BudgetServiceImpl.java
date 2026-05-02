package com.hazine.govbudget.service.impl;

import com.hazine.govbudget.domain.entity.Budget;
import com.hazine.govbudget.domain.entity.Department;
import com.hazine.govbudget.domain.entity.User;
import com.hazine.govbudget.domain.enums.AuditAction;
import com.hazine.govbudget.domain.enums.BudgetStatus;
import com.hazine.govbudget.domain.repository.BudgetRepository;
import com.hazine.govbudget.domain.repository.DepartmentRepository;
import com.hazine.govbudget.domain.repository.UserRepository;
import com.hazine.govbudget.dto.request.BudgetCreateRequest;
import com.hazine.govbudget.dto.request.BudgetUpdateRequest;
import com.hazine.govbudget.dto.response.BudgetResponse;
import com.hazine.govbudget.dto.response.PageResponse;
import com.hazine.govbudget.exception.BusinessException;
import com.hazine.govbudget.exception.ResourceNotFoundException;
import com.hazine.govbudget.service.AuditLogService;
import com.hazine.govbudget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public BudgetResponse create(BudgetCreateRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(
                    "Bitiş tarihi başlangıç tarihinden önce olamaz",
                    "INVALID_DATE_RANGE");
        }

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departman", request.getDepartmentId()));

        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı"));

        Budget budget = Budget.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .remainingAmount(request.getTotalAmount())
                .fiscalYear(request.getFiscalYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .department(department)
                .createdByUser(currentUser)
                .status(BudgetStatus.DRAFT)
                .build();

        Budget saved = budgetRepository.save(budget);
        auditLogService.log("Budget", saved.getId(), AuditAction.CREATE,
                null, saved.getTitle(), "Bütçe oluşturuldu");
        log.info("Budget created: {} for department: {}",
                saved.getId(), department.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse update(Long id, BudgetUpdateRequest request) {
        Budget budget = findById(id);
        if (budget.getStatus() != BudgetStatus.DRAFT) {
            throw new BusinessException(
                    "Sadece taslak bütçeler güncellenebilir",
                    "BUDGET_NOT_EDITABLE");
        }
        String oldValue = budget.getTitle();
        if (request.getTitle() != null) budget.setTitle(request.getTitle());
        if (request.getDescription() != null)
            budget.setDescription(request.getDescription());
        if (request.getTotalAmount() != null) {
            budget.setTotalAmount(request.getTotalAmount());
            budget.setRemainingAmount(
                    request.getTotalAmount().subtract(budget.getSpentAmount()));
        }
        if (request.getStartDate() != null) budget.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) budget.setEndDate(request.getEndDate());

        Budget saved = budgetRepository.save(budget);
        auditLogService.log("Budget", saved.getId(), AuditAction.UPDATE,
                oldValue, saved.getTitle(), "Bütçe güncellendi");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BudgetResponse> getAll(int page, int size, String sortBy) {
        Page<Budget> budgetPage = budgetRepository.findAll(
                PageRequest.of(page, size, Sort.by(sortBy).descending()));
        return PageResponse.<BudgetResponse>builder()
                .content(budgetPage.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .pageNumber(budgetPage.getNumber())
                .pageSize(budgetPage.getSize())
                .totalElements(budgetPage.getTotalElements())
                .totalPages(budgetPage.getTotalPages())
                .last(budgetPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getByDepartment(Long departmentId) {
        return budgetRepository.findByDepartmentId(departmentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getByFiscalYear(Integer year) {
        return budgetRepository.findByFiscalYear(year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetResponse approve(Long id) {
        Budget budget = findById(id);
        if (budget.getStatus() != BudgetStatus.PENDING) {
            throw new BusinessException(
                    "Sadece onay bekleyen bütçeler onaylanabilir",
                    "INVALID_STATUS_TRANSITION");
        }
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User approver = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı"));
        budget.setStatus(BudgetStatus.APPROVED);
        budget.setApprovedByUser(approver);
        Budget saved = budgetRepository.save(budget);
        auditLogService.log("Budget", saved.getId(), AuditAction.APPROVE,
                BudgetStatus.PENDING.name(), BudgetStatus.APPROVED.name(),
                "Bütçe onaylandı");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse reject(Long id, String reason) {
        Budget budget = findById(id);
        if (budget.getStatus() != BudgetStatus.PENDING) {
            throw new BusinessException(
                    "Sadece onay bekleyen bütçeler reddedilebilir",
                    "INVALID_STATUS_TRANSITION");
        }
        budget.setStatus(BudgetStatus.REJECTED);
        Budget saved = budgetRepository.save(budget);
        auditLogService.log("Budget", saved.getId(), AuditAction.REJECT,
                BudgetStatus.PENDING.name(), BudgetStatus.REJECTED.name(), reason);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse activate(Long id) {
        Budget budget = findById(id);
        if (budget.getStatus() != BudgetStatus.APPROVED) {
            throw new BusinessException(
                    "Sadece onaylanmış bütçeler aktive edilebilir",
                    "INVALID_STATUS_TRANSITION");
        }
        budget.setStatus(BudgetStatus.ACTIVE);
        Budget saved = budgetRepository.save(budget);
        auditLogService.log("Budget", saved.getId(), AuditAction.UPDATE,
                BudgetStatus.APPROVED.name(), BudgetStatus.ACTIVE.name(),
                "Bütçe aktive edildi");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse close(Long id) {
        Budget budget = findById(id);
        budget.setStatus(BudgetStatus.CLOSED);
        Budget saved = budgetRepository.save(budget);
        auditLogService.log("Budget", saved.getId(), AuditAction.UPDATE,
                BudgetStatus.ACTIVE.name(), BudgetStatus.CLOSED.name(),
                "Bütçe kapatıldı");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Budget budget = findById(id);
        if (budget.getStatus() != BudgetStatus.DRAFT) {
            throw new BusinessException(
                    "Sadece taslak bütçeler silinebilir",
                    "BUDGET_NOT_DELETABLE");
        }
        budget.setDeleted(true);
        budgetRepository.save(budget);
        auditLogService.log("Budget", id, AuditAction.DELETE,
                budget.getTitle(), null, "Bütçe silindi");
    }

    private Budget findById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bütçe", id));
    }

    private BudgetResponse toResponse(Budget b) {
        return BudgetResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .description(b.getDescription())
                .totalAmount(b.getTotalAmount())
                .spentAmount(b.getSpentAmount())
                .remainingAmount(b.getRemainingAmount())
                .fiscalYear(b.getFiscalYear())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .status(b.getStatus())
                .departmentName(b.getDepartment().getName())
                .departmentCode(b.getDepartment().getCode())
                .createdByUsername(b.getCreatedByUser() != null
                        ? b.getCreatedByUser().getUsername() : null)
                .approvedByUsername(b.getApprovedByUser() != null
                        ? b.getApprovedByUser().getUsername() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}