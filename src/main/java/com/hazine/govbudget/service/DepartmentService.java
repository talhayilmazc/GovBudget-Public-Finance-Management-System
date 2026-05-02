package com.hazine.govbudget.service;

import com.hazine.govbudget.dto.request.DepartmentRequest;
import com.hazine.govbudget.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);
    DepartmentResponse update(Long id, DepartmentRequest request);
    DepartmentResponse getById(Long id);
    List<DepartmentResponse> getAll();
    List<DepartmentResponse> getSubDepartments(Long parentId);
    void delete(Long id);
}