package com.hinchmart.controller;

import com.hinchmart.dto.request.LoginRequest;
import com.hinchmart.dto.request.RefreshTokenRequest;
import com.hinchmart.dto.request.RegisterRequest;
import com.hinchmart.dto.request.SendOtpRequest;
import com.hinchmart.dto.request.VerifyOtpRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.AuthResponse;
import com.hinchmart.dto.response.UserDto;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication & Authorization", description = "Endpoints for Buyer/Seller Registration, Login, OTP, Refresh Token and Session Management")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new User (Buyer or Seller)", description = "Creates a new user account with BUYER or SELLER role and initialized profile.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with Email or Phone", description = "Authenticates user credentials and returns JWT access & refresh tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to Phone/Email", description = "Generates and sends a 6-digit one-time password.")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String otp = otpService.generateAndSendOtp(request.getIdentifier(), request.getPurpose());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully to " + request.getIdentifier(), "OTP: " + otp));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and Login", description = "Validates the OTP and returns authenticated session tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT Access Token", description = "Generates a new access token using a valid refresh token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Invalidates the refresh token and ends the active session.")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current Authenticated User", description = "Returns full profile of the logged-in user.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(ApiResponse.error("Unauthenticated"), HttpStatus.UNAUTHORIZED);
        }
        UserDto userDto = authService.getCurrentUserDto(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }
}
