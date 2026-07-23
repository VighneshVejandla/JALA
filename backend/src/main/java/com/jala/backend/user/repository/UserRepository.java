package com.jala.backend.user.repository;

import com.jala.backend.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Free-text search over name and employee code. */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY u.fullName
            """)
    List<User> search(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmployeeCode(String employeeCode);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByRole_Name(String roleName);

    @Override
    @EntityGraph(attributePaths = "role")
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = "role")
    Optional<User> findById(UUID id);
}