package com.hinchmart.repository;

import com.hinchmart.entity.BuyerProfile;
import com.hinchmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuyerProfileRepository extends JpaRepository<BuyerProfile, Long> {
    Optional<BuyerProfile> findByUser(User user);
    Optional<BuyerProfile> findByUserId(Long userId);
    Optional<BuyerProfile> findByGstin(String gstin);
}
