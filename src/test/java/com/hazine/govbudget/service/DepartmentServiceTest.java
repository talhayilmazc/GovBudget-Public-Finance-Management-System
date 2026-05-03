package com.hazine.govbudget.service;

import com.hazine.govbudget.domain.entity.Department;
import com.hazine.govbudget.domain.repository.DepartmentRepository;
import com.hazine.govbudget.dto.request.DepartmentRequest;
import com.hazine.govbudget.dto.response.DepartmentResponse;
import com.hazine.govbudget.exception.BusinessException;
import com.hazine.govbudget.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Department Service Tests")
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department testDepartment;
    private DepartmentRequest createRequest;

    @BeforeEach
    void setUp() {
        testDepartment = Department.builder()
                .name("Bilgi İşlem")
                .code("IT-001")
                .description("IT Departmanı")
                .build();

        createRequest = DepartmentRequest.builder()
                .name("Bilgi İşlem")
                .code("IT-001")
                .description("IT Departmanı")
                .build();
    }

    @Test
    @DisplayName("Departman başarıyla oluşturulmalı")
    void create_ShouldCreateDepartment_WhenValidRequest() {
        when(departmentRepository.existsByCode("IT-001")).thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenReturn(testDepartment);

        DepartmentResponse response = departmentService.create(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Bilgi İşlem");
        assertThat(response.getCode()).isEqualTo("IT-001");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    @DisplayName("Var olan kod ile departman oluşturulamaz")
    void create_ShouldThrowException_WhenCodeAlreadyExists() {
        when(departmentRepository.existsByCode("IT-001")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("kod");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tüm departmanlar listelenmeli")
    void getAll_ShouldReturnAllDepartments() {
        when(departmentRepository.findAllActive())
                .thenReturn(List.of(testDepartment));

        List<DepartmentResponse> responses = departmentService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCode()).isEqualTo("IT-001");
    }

    @Test
    @DisplayName("Departman soft delete ile silinmeli")
    void delete_ShouldSoftDeleteDepartment() {
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(testDepartment));
        when(departmentRepository.save(any(Department.class)))
                .thenReturn(testDepartment);

        departmentService.delete(1L);

        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(auditLogService, times(1)).log(any(), any(), any(), any(), any(), any());
    }
}