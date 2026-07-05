package com.jala.backend.user.repository;

import com.jala.backend.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

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