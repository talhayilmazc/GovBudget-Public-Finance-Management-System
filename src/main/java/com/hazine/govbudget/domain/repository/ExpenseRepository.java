package com.hazine.govbudget.domain.repository;

import com.hazine.govbudget.domain.entity.Expense;
import com.hazine.govbudget.domain.enums.ExpenseCategory;
import com.hazine.govbudget.domain.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {

    List<Expense> findByBudgetId(Long budgetId);

    List<Expense> findByStatus(ExpenseStatus status);

    List<Expense> findBySubmittedById(Long userId);

    Optional<Expense> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    List<Expense> findByBudgetIdAndStatus(Long budgetId, ExpenseStatus status);

    List<Expense> findByCategory(ExpenseCategory category);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.budget.id = :budgetId " +
           "AND e.status = 'APPROVED' AND e.deleted = false")
    BigDecimal getTotalApprovedExpenseByBudget(@Param("budgetId") Long budgetId);

    @Query("SELECT e FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end " +
           "AND e.deleted = false")
    List<Expense> findByDateRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}