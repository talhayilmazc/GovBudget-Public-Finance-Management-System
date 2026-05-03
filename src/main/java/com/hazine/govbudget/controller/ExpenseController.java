package com.hazine.govbudget.controller;

import com.hazine.govbudget.dto.request.ExpenseCreateRequest;
import com.hazine.govbudget.dto.request.ExpenseUpdateRequest;
import com.hazine.govbudget.dto.response.ExpenseResponse;
import com.hazine.govbudget.dto.response.PageResponse;
import com.hazine.govbudget.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Harcama yönetimi")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER', 'FINANCE_OFFICER')")
    @Operation(summary = "Harcama oluştur")
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER', 'FINANCE_OFFICER')")
    @Operation(summary = "Harcama güncelle")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Harcama getir")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Harcamaları listele")
    public ResponseEntity<PageResponse<ExpenseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(expenseService.getAll(page, size, sortBy));
    }

    @GetMapping("/budget/{budgetId}")
    @Operation(summary = "Bütçeye göre harcamaları listele")
    public ResponseEntity<List<ExpenseResponse>> getByBudget(
            @PathVariable Long budgetId) {
        return ResponseEntity.ok(expenseService.getByBudget(budgetId));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Harcama onayla")
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Harcama reddet")
    public ResponseEntity<ExpenseResponse> reject(
            @PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(expenseService.reject(id, reason));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Harcama sil")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}