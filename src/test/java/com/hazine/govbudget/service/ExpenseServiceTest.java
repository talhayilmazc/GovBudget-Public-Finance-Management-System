package com.hazine.govbudget.service;

import com.hazine.govbudget.domain.entity.Budget;
import com.hazine.govbudget.domain.entity.Department;
import com.hazine.govbudget.domain.entity.Expense;
import com.hazine.govbudget.domain.entity.User;
import com.hazine.govbudget.domain.enums.BudgetStatus;
import com.hazine.govbudget.domain.enums.ExpenseCategory;
import com.hazine.govbudget.domain.enums.ExpenseStatus;
import com.hazine.govbudget.domain.repository.BudgetRepository;
import com.hazine.govbudget.domain.repository.ExpenseRepository;
import com.hazine.govbudget.domain.repository.UserRepository;
import com.hazine.govbudget.dto.request.ExpenseCreateRequest;
import com.hazine.govbudget.dto.response.ExpenseResponse;
import com.hazine.govbudget.exception.BusinessException;
import com.hazine.govbudget.exception.ResourceNotFoundException;
import com.hazine.govbudget.service.impl.ExpenseServiceImpl;
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
@DisplayName("Expense Service Tests")
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Budget testBudget;
    private User testUser;
    private Expense testExpense;
    private ExpenseCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder()
                .name("Bilgi İşlem").code("IT-001").build();

        testUser = User.builder()
                .username("testuser")
                .email("test@hazine.gov.tr")
                .firstName("Test").lastName("User")
                .build();

        testBudget = Budget.builder()
                .title("2025 IT Bütçesi")
                .totalAmount(new BigDecimal("100000.00"))
                .spentAmount(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("100000.00"))
                .status(BudgetStatus.ACTIVE)
                .department(dept)
                .build();

        testExpense = Expense.builder()
                .title("Laptop Alımı")
                .amount(new BigDecimal("5000.00"))
                .expenseDate(LocalDate.now())
                .referenceNumber("REF-001")
                .category(ExpenseCategory.EQUIPMENT)
                .status(ExpenseStatus.PENDING)
                .budget(testBudget)
                .submittedBy(testUser)
                .build();

        createRequest = ExpenseCreateRequest.builder()
                .title("Laptop Alımı")
                .amount(new BigDecimal("5000.00"))
                .expenseDate(LocalDate.now())
                .referenceNumber("REF-001")
                .category(ExpenseCategory.EQUIPMENT)
                .budgetId(1L)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Harcama başarıyla oluşturulmalı")
    void create_ShouldCreateExpense_WhenValidRequest() {
        when(expenseRepository.existsByReferenceNumber("REF-001"))
                .thenReturn(false);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.of(testBudget));
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(testExpense);

        ExpenseResponse response = expenseService.create(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Laptop Alımı");
        assertThat(response.getStatus()).isEqualTo(ExpenseStatus.PENDING);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("Duplicate referans numarası hata fırlatmalı")
    void create_ShouldThrowException_WhenDuplicateReferenceNumber() {
        when(expenseRepository.existsByReferenceNumber("REF-001"))
                .thenReturn(true);

        assertThatThrownBy(() -> expenseService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("referans");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Aktif olmayan bütçeye harcama eklenemez")
    void create_ShouldThrowException_WhenBudgetNotActive() {
        testBudget.setStatus(BudgetStatus.DRAFT);
        when(expenseRepository.existsByReferenceNumber(any()))
                .thenReturn(false);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> expenseService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aktif");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bütçe yetersizse harcama oluşturulamaz")
    void create_ShouldThrowException_WhenInsufficientBudget() {
        testBudget.setRemainingAmount(new BigDecimal("1000.00"));
        when(expenseRepository.existsByReferenceNumber(any()))
                .thenReturn(false);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> expenseService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Kalan");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Harcama onaylandığında bütçe bakiyesi güncellenmeli")
    void approve_ShouldUpdateBudgetBalance_WhenExpenseApproved() {
        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(testExpense));
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(testExpense);

        expenseService.approve(1L);

        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("Bütçe bulunamazsa hata fırlatmalı")
    void create_ShouldThrowException_WhenBudgetNotFound() {
        when(expenseRepository.existsByReferenceNumber(any()))
                .thenReturn(false);
        when(budgetRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository, never()).save(any());
    }
}