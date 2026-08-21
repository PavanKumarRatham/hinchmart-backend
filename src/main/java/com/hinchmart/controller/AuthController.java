package com.hinchmart.controller;

import com.hinchmart.dto.request.LoginRequest;
import com.hinchmart.dto.request.RegisterRequest;
import com.hinchmart.dto.request.SendOtpRequest;
import com.hinchmart.dto.request.VerifyOtpRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.UserDto;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for Buyer/Seller Registration, Password Login, and OTP Verification")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new User (Buyer or Seller)", description = "Creates a new user account with BUYER or SELLER role and initialized profile.")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserDto response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with Email or Phone", description = "Authenticates user credentials and returns the user profile.")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequest request) {
        UserDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to Phone/Email", description = "Generates and sends a 6-digit one-time password.")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String otp = otpService.generateAndSendOtp(request.getIdentifier(), request.getPurpose());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully to " + request.getIdentifier(), "OTP: " + otp));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and Login", description = "Validates the OTP and returns the user profile.")
    public ResponseEntity<ApiResponse<UserDto>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        UserDto response = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }
}
