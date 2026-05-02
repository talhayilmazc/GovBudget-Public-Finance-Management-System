package com.hazine.govbudget.domain.repository;

import com.hazine.govbudget.domain.entity.Budget;
import com.hazine.govbudget.domain.enums.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>,
        JpaSpecificationExecutor<Budget> {

    List<Budget> findByDepartmentId(Long departmentId);

    List<Budget> findByFiscalYear(Integer fiscalYear);

    List<Budget> findByStatus(BudgetStatus status);

    List<Budget> findByDepartmentIdAndFiscalYear(Long departmentId, Integer fiscalYear);

    @Query("SELECT b FROM Budget b WHERE b.department.id = :deptId " +
           "AND b.fiscalYear = :year AND b.status = :status AND b.deleted = false")
    List<Budget> findByDepartmentAndYearAndStatus(
            @Param("deptId") Long departmentId,
            @Param("year") Integer fiscalYear,
            @Param("status") BudgetStatus status);

    @Query("SELECT SUM(b.totalAmount) FROM Budget b WHERE b.fiscalYear = :year " +
           "AND b.status = 'ACTIVE' AND b.deleted = false")
    java.math.BigDecimal getTotalBudgetByYear(@Param("year") Integer fiscalYear);
}