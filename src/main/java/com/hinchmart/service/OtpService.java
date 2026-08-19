package com.hinchmart.service;

import com.hinchmart.entity.OtpVerification;
import com.hinchmart.entity.enums.OtpPurpose;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpVerificationRepository otpVerificationRepository;

    @Value("${hinchmart.otp.expiration-minutes:10}")
    private int expirationMinutes;

    @Value("${hinchmart.otp.default-test-code:123456}")
    private String defaultTestCode;

    public OtpService(OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
    }

    @Transactional
    public String generateAndSendOtp(String identifier, OtpPurpose purpose) {
        // Generate a 6-digit numeric OTP code
        String otpCode;
        if ("true".equalsIgnoreCase(System.getProperty("hinchmart.otp.mock", "true"))) {
            otpCode = defaultTestCode;
        } else {
            SecureRandom random = new SecureRandom();
            int num = 100000 + random.nextInt(900000);
            otpCode = String.valueOf(num);
        }

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(expirationMinutes);

        OtpVerification otpVerification = new OtpVerification(identifier, otpCode, purpose, expiryTime);
        otpVerificationRepository.save(otpVerification);

        // In production, integrate SMS / Email gateway (e.g., Twilio, AWS SNS, SendGrid)
        logger.info(">>> [OTP DISPATCH] Generated OTP: {} for identifier: {} (Purpose: {}, Expires: {})",
                otpCode, identifier, purpose, expiryTime);

        return otpCode;
    }

    @Transactional
    public boolean verifyOtp(String identifier, String otpCode, OtpPurpose purpose) {
        OtpVerification otp = otpVerificationRepository
                .findTopByIdentifierAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(identifier, otpCode)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP code"));

        if (otp.isExpired()) {
            throw new BadRequestException("OTP code has expired. Please request a new one.");
        }

        if (purpose != null && otp.getPurpose() != purpose) {
            throw new BadRequestException("OTP was not issued for the requested action.");
        }

        otp.setUsed(true);
        otpVerificationRepository.save(otp);
        return true;
    }
}
