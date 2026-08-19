package com.hinchmart.repository;

import com.hinchmart.entity.SellerProfile;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUser(User user);
    Optional<SellerProfile> findByUserId(Long userId);
    List<SellerProfile> findByStatus(SellerStatus status);
    long countByStatus(SellerStatus status);
    Optional<SellerProfile> findByGstin(String gstin);
}
