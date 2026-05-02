package com.hazine.govbudget.service;

import com.hazine.govbudget.dto.request.BudgetCreateRequest;
import com.hazine.govbudget.dto.request.BudgetUpdateRequest;
import com.hazine.govbudget.dto.response.BudgetResponse;
import com.hazine.govbudget.dto.response.PageResponse;

import java.util.List;

public interface BudgetService {
    BudgetResponse create(BudgetCreateRequest request);
    BudgetResponse update(Long id, BudgetUpdateRequest request);
    BudgetResponse getById(Long id);
    PageResponse<BudgetResponse> getAll(int page, int size, String sortBy);
    List<BudgetResponse> getByDepartment(Long departmentId);
    List<BudgetResponse> getByFiscalYear(Integer year);
    BudgetResponse approve(Long id);
    BudgetResponse reject(Long id, String reason);
    BudgetResponse activate(Long id);
    BudgetResponse close(Long id);
    void delete(Long id);
}