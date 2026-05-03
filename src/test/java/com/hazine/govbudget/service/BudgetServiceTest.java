package com.hazine.govbudget.service;

import com.hazine.govbudget.domain.entity.Budget;
import com.hazine.govbudget.domain.entity.Department;
import com.hazine.govbudget.domain.entity.User;
import com.hazine.govbudget.domain.enums.BudgetStatus;
import com.hazine.govbudget.domain.repository.BudgetRepository;
import com.hazine.govbudget.domain.repository.DepartmentRepository;
import com.hazine.govbudget.domain.repository.UserRepository;
import com.hazine.govbudget.dto.request.BudgetCreateRequest;
import com.hazine.govbudget.dto.response.BudgetResponse;
import com.hazine.govbudget.exception.BusinessException;
import com.hazine.govbudget.exception.ResourceNotFoundException;
import com.hazine.govbudget.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@DisplayName("Budget Service Tests")
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Department testDepartment;
    private User testUser;
    private Budget testBudget;
    private BudgetCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testDepartment = Department.builder()
                .name("Bilgi İşlem")
                .code("IT-001")
                .build();

        testUser = User.builder()
                .username("testuser")
                .email("test@hazine.gov.tr")
                .firstName("Test")
                .lastName("User")
                .build();

        testBudget = Budget.builder()
                .title("2025 IT Bütçesi")
                .totalAmount(new BigDecimal("100000.00"))
                .spentAmount(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("100000.00"))
                .fiscalYear(2025)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(BudgetStatus.DRAFT)
                .department(testDepartment)
                .createdByUser(testUser)
                .build();

        createRequest = BudgetCreateRequest.builder()
                .title("2025 IT Bütçesi")
                .totalAmount(new BigDecimal("100000.00"))
                .fiscalYear(2025)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .departmentId(1L)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Bütçe başarıyla oluşturulmalı")
    void create_ShouldCreateBudget_WhenValidRequest() {
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(testDepartment));
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(testBudget);

        BudgetResponse response = budgetService.create(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("2025 IT Bütçesi");
        assertThat(response.getStatus()).isEqualTo(BudgetStatus.DRAFT);
        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(auditLogService, times(1)).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Departman bulunamazsa hata fırlatmalı")
    void create_ShouldThrowException_WhenDepartmentNotFound() {
        when(departmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bitiş tarihi başlangıçtan önce ise hata fırlatmalı")
    void create_ShouldThrowException_WhenEndDateBeforeStartDate() {
        createRequest.setEndDate(LocalDate.of(2024, 12, 31));
        createRequest.setStartDate(LocalDate.of(2025, 1, 1));

        assertThatThrownBy(() -> budgetService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bitiş tarihi");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("DRAFT olmayan bütçe güncellenemez")
    void update_ShouldThrowException_WhenBudgetNotDraft() {
        testBudget.setStatus(BudgetStatus.ACTIVE);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> budgetService.update(1L, any()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taslak");
    }

    @Test
    @DisplayName("PENDING bütçe onaylanabilmeli")
    void approve_ShouldApproveBudget_WhenStatusIsPending() {
        testBudget.setStatus(BudgetStatus.PENDING);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.of(testBudget));
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(testBudget);

        BudgetResponse response = budgetService.approve(1L);

        assertThat(response).isNotNull();
        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(auditLogService, times(1)).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("DRAFT olmayan bütçe silinememeli")
    void delete_ShouldThrowException_WhenBudgetNotDraft() {
        testBudget.setStatus(BudgetStatus.ACTIVE);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> budgetService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taslak");

        verify(budgetRepository, never()).save(any());
    }
}