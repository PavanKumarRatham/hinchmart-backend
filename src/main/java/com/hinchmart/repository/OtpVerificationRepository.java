package com.hinchmart.repository;

import com.hinchmart.entity.OtpVerification;
import com.hinchmart.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByIdentifierAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(String identifier, String otpCode);
    Optional<OtpVerification> findTopByIdentifierAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(String identifier, OtpPurpose purpose);
}
