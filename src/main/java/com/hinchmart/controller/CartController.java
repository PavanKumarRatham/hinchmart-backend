package com.hinchmart.controller;

import com.hinchmart.dto.request.AddToCartRequest;
import com.hinchmart.dto.request.UpdateCartItemRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.CartDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart & B2B Calculations (Member 2)", description = "Endpoints for Buyer Shopping Cart, MOQ Validations, Bulk Pricing Tier Calculations, and GST Breakdown")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get Current User Cart",
            description = "Returns current shopping cart with dynamic bulk pricing tiers, MOQ checks, line item subtotals, GST breakdown, and grand total.")
    public ResponseEntity<ApiResponse<CartDto>> getCart(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        CartDto cart = cartService.getCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add Item to Cart",
            description = "Adds a product to the cart. Validates: Quantity >= MOQ, Seller Active & Approved, Product Active & Approved, Stock Availability. Automatically calculates bulk pricing.")
    public ResponseEntity<ApiResponse<CartDto>> addItem(Authentication authentication,
                                                        @Valid @RequestBody AddToCartRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        CartDto cart = cartService.addToCart(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Item added to cart", cart), HttpStatus.CREATED);
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update Cart Item Quantity",
            description = "Updates the quantity of an item in the cart and recalculates bulk pricing tier and GST.")
    public ResponseEntity<ApiResponse<CartDto>> updateItem(Authentication authentication,
                                                           @PathVariable Long id,
                                                           @Valid @RequestBody UpdateCartItemRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        CartDto cart = cartService.updateCartItem(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove Item from Cart", description = "Removes a specific line item from the cart.")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(Authentication authentication,
                                                           @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        CartDto cart = cartService.removeCartItem(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear Cart", description = "Removes all items from the current user's shopping cart.")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}
