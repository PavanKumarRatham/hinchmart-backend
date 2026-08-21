package com.hinchmart.controller;

import com.hinchmart.dto.request.BuyerProfileUpdateRequest;
import com.hinchmart.dto.request.SellerProfileUpdateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.BuyerProfileDto;
import com.hinchmart.dto.response.SellerProfileDto;
import com.hinchmart.dto.response.UserDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User & Profile Management", description = "Endpoints for Buyer and Seller Profile Management")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/profile/buyer")
    @Operation(summary = "Get Buyer Profile", description = "Returns the buyer profile for the specified user.")
    public ResponseEntity<ApiResponse<BuyerProfileDto>> getBuyerProfile(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        BuyerProfileDto profile = userService.getBuyerProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile/buyer")
    @Operation(summary = "Update Buyer Profile", description = "Updates buyer business details, GSTIN, and addresses.")
    public ResponseEntity<ApiResponse<BuyerProfileDto>> updateBuyerProfile(@RequestParam Long userId,
                                                                          @RequestBody BuyerProfileUpdateRequest request) {
        User user = authService.getUserById(userId);
        BuyerProfileDto updated = userService.updateBuyerProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Buyer profile updated successfully", updated));
    }

    @GetMapping("/profile/seller")
    @Operation(summary = "Get Current Seller Profile", description = "Returns seller profile, warehouse, and KYC status.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> getSellerProfile(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        SellerProfileDto profile = userService.getSellerProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile/seller")
    @Operation(summary = "Update Seller Profile", description = "Updates seller company name, warehouse location, PAN and GSTIN.")
    public ResponseEntity<ApiResponse<SellerProfileDto>> updateSellerProfile(@RequestParam Long userId,
                                                                            @RequestBody SellerProfileUpdateRequest request) {
        User user = authService.getUserById(userId);
        SellerProfileDto updated = userService.updateSellerProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Seller profile updated successfully", updated));
    }
}
