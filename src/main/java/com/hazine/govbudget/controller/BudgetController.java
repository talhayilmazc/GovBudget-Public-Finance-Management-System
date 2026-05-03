package com.hazine.govbudget.controller;

import com.hazine.govbudget.dto.request.BudgetCreateRequest;
import com.hazine.govbudget.dto.request.BudgetUpdateRequest;
import com.hazine.govbudget.dto.response.BudgetResponse;
import com.hazine.govbudget.dto.response.PageResponse;
import com.hazine.govbudget.service.BudgetService;
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
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Bütçe yönetimi")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Bütçe oluştur")
    public ResponseEntity<BudgetResponse> create(
            @Valid @RequestBody BudgetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Bütçe güncelle")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateRequest request) {
        return ResponseEntity.ok(budgetService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bütçe getir")
    public ResponseEntity<BudgetResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Bütçeleri listele")
    public ResponseEntity<PageResponse<BudgetResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(budgetService.getAll(page, size, sortBy));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Departmana göre bütçeleri listele")
    public ResponseEntity<List<BudgetResponse>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(budgetService.getByDepartment(departmentId));
    }

    @GetMapping("/fiscal-year/{year}")
    @Operation(summary = "Mali yıla göre bütçeleri listele")
    public ResponseEntity<List<BudgetResponse>> getByFiscalYear(
            @PathVariable Integer year) {
        return ResponseEntity.ok(budgetService.getByFiscalYear(year));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Bütçe onayla")
    public ResponseEntity<BudgetResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Bütçe reddet")
    public ResponseEntity<BudgetResponse> reject(
            @PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(budgetService.reject(id, reason));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Bütçe aktive et")
    public ResponseEntity<BudgetResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.activate(id));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUDGET_MANAGER')")
    @Operation(summary = "Bütçe kapat")
    public ResponseEntity<BudgetResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.close(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bütçe sil")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}