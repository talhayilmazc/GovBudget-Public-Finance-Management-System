package com.hazine.govbudget.domain.repository;

import com.hazine.govbudget.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByDepartmentId(Long departmentId);

    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.active = true")
    List<User> findAllActiveUsers();
}