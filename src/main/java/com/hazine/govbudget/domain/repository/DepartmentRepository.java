package com.hazine.govbudget.domain.repository;

import com.hazine.govbudget.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    List<Department> findByParentIsNull();

    List<Department> findByParentId(Long parentId);

    @Query("SELECT d FROM Department d WHERE d.deleted = false ORDER BY d.name")
    List<Department> findAllActive();
}