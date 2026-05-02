package com.hazine.govbudget.service;

import com.hazine.govbudget.dto.request.ExpenseCreateRequest;
import com.hazine.govbudget.dto.request.ExpenseUpdateRequest;
import com.hazine.govbudget.dto.response.ExpenseResponse;
import com.hazine.govbudget.dto.response.PageResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse create(ExpenseCreateRequest request);
    ExpenseResponse update(Long id, ExpenseUpdateRequest request);
    ExpenseResponse getById(Long id);
    PageResponse<ExpenseResponse> getAll(int page, int size, String sortBy);
    List<ExpenseResponse> getByBudget(Long budgetId);
    ExpenseResponse approve(Long id);
    ExpenseResponse reject(Long id, String reason);
    void delete(Long id);
}