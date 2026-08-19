package com.hinchmart.repository;

import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.AccountStatus;
import com.hinchmart.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(Role role);
    List<User> findByStatus(AccountStatus status);
    long countByRole(Role role);
}
