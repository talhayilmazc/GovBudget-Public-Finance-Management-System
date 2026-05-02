package com.hazine.govbudget.service.impl;

import com.hazine.govbudget.domain.entity.Budget;
import com.hazine.govbudget.domain.entity.Expense;
import com.hazine.govbudget.domain.entity.User;
import com.hazine.govbudget.domain.enums.AuditAction;
import com.hazine.govbudget.domain.enums.BudgetStatus;
import com.hazine.govbudget.domain.enums.ExpenseStatus;
import com.hazine.govbudget.domain.repository.BudgetRepository;
import com.hazine.govbudget.domain.repository.ExpenseRepository;
import com.hazine.govbudget.domain.repository.UserRepository;
import com.hazine.govbudget.dto.request.ExpenseCreateRequest;
import com.hazine.govbudget.dto.request.ExpenseUpdateRequest;
import com.hazine.govbudget.dto.response.ExpenseResponse;
import com.hazine.govbudget.dto.response.PageResponse;
import com.hazine.govbudget.exception.BusinessException;
import com.hazine.govbudget.exception.ResourceNotFoundException;
import com.hazine.govbudget.service.AuditLogService;
import com.hazine.govbudget.service.ExpenseService;
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
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        if (expenseRepository.existsByReferenceNumber(request.getReferenceNumber())) {
            throw new BusinessException(
                    "Bu referans numarası zaten kullanılıyor: "
                            + request.getReferenceNumber(),
                    "DUPLICATE_REFERENCE");
        }

        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bütçe", request.getBudgetId()));

        if (budget.getStatus() != BudgetStatus.ACTIVE) {
            throw new BusinessException(
                    "Harcama sadece aktif bütçelere eklenebilir",
                    "BUDGET_NOT_ACTIVE");
        }

        if (request.getAmount().compareTo(budget.getRemainingAmount()) > 0) {
            throw new BusinessException(
                    "Harcama tutarı bütçenin kalan miktarını aşıyor. " +
                    "Kalan: " + budget.getRemainingAmount(),
                    "INSUFFICIENT_BUDGET");
        }

        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı"));

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .referenceNumber(request.getReferenceNumber())
                .category(request.getCategory())
                .invoiceNumber(request.getInvoiceNumber())
                .budget(budget)
                .submittedBy(currentUser)
                .status(ExpenseStatus.PENDING)
                .build();

        Expense saved = expenseRepository.save(expense);
        auditLogService.log("Expense", saved.getId(), AuditAction.CREATE,
                null, saved.getTitle(), "Harcama kaydı oluşturuldu");
        log.info("Expense created: {} for budget: {}",
                saved.getId(), budget.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        Expense expense = findById(id);
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BusinessException(
                    "Sadece beklemedeki harcamalar güncellenebilir",
                    "EXPENSE_NOT_EDITABLE");
        }
        if (request.getTitle() != null) expense.setTitle(request.getTitle());
        if (request.getDescription() != null)
            expense.setDescription(request.getDescription());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getExpenseDate() != null)
            expense.setExpenseDate(request.getExpenseDate());
        if (request.getCategory() != null) expense.setCategory(request.getCategory());
        if (request.getInvoiceNumber() != null)
            expense.setInvoiceNumber(request.getInvoiceNumber());

        Expense saved = expenseRepository.save(expense);
        auditLogService.log("Expense", saved.getId(), AuditAction.UPDATE,
                null, saved.getTitle(), "Harcama güncellendi");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getAll(int page, int size, String sortBy) {
        Page<Expense> expensePage = expenseRepository.findAll(
                PageRequest.of(page, size, Sort.by(sortBy).descending()));
        return PageResponse.<ExpenseResponse>builder()
                .content(expensePage.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .pageNumber(expensePage.getNumber())
                .pageSize(expensePage.getSize())
                .totalElements(expensePage.getTotalElements())
                .totalPages(expensePage.getTotalPages())
                .last(expensePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getByBudget(Long budgetId) {
        return expenseRepository.findByBudgetId(budgetId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExpenseResponse approve(Long id) {
        Expense expense = findById(id);
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BusinessException(
                    "Sadece beklemedeki harcamalar onaylanabilir",
                    "INVALID_STATUS_TRANSITION");
        }
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User approver = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı"));

        // Bütçeden düş
        Budget budget = expense.getBudget();
        budget.setSpentAmount(budget.getSpentAmount().add(expense.getAmount()));
        budget.setRemainingAmount(
                budget.getRemainingAmount().subtract(expense.getAmount()));
        budgetRepository.save(budget);

        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(approver);
        Expense saved = expenseRepository.save(expense);
        auditLogService.log("Expense", saved.getId(), AuditAction.APPROVE,
                ExpenseStatus.PENDING.name(), ExpenseStatus.APPROVED.name(),
                "Harcama onaylandı");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse reject(Long id, String reason) {
        Expense expense = findById(id);
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BusinessException(
                    "Sadece beklemedeki harcamalar reddedilebilir",
                    "INVALID_STATUS_TRANSITION");
        }
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setRejectionReason(reason);
        Expense saved = expenseRepository.save(expense);
        auditLogService.log("Expense", saved.getId(), AuditAction.REJECT,
                ExpenseStatus.PENDING.name(), ExpenseStatus.REJECTED.name(), reason);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Expense expense = findById(id);
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BusinessException(
                    "Sadece beklemedeki harcamalar silinebilir",
                    "EXPENSE_NOT_DELETABLE");
        }
        expense.setDeleted(true);
        expenseRepository.save(expense);
        auditLogService.log("Expense", id, AuditAction.DELETE,
                expense.getTitle(), null, "Harcama silindi");
    }

    private Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Harcama", id));
    }

    private ExpenseResponse toResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .amount(e.getAmount())
                .expenseDate(e.getExpenseDate())
                .referenceNumber(e.getReferenceNumber())
                .category(e.getCategory())
                .status(e.getStatus())
                .budgetTitle(e.getBudget().getTitle())
                .budgetId(e.getBudget().getId())
                .submittedByUsername(e.getSubmittedBy().getUsername())
                .approvedByUsername(e.getApprovedBy() != null
                        ? e.getApprovedBy().getUsername() : null)
                .rejectionReason(e.getRejectionReason())
                .invoiceNumber(e.getInvoiceNumber())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}