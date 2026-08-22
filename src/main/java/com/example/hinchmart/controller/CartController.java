package com.example.hinchmart.controller;

import com.example.hinchmart.dto.CartItemRequest;
import com.example.hinchmart.dto.CartResponse;
import com.example.hinchmart.dto.UpdateCartItemRequest;
import com.example.hinchmart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * GET /api/cart
     * Retrieve the current buyer's cart with calculated bulk pricing, GST, and totals.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestParam(required = false, defaultValue = "1") Long buyerId) {
        CartResponse cart = cartService.getCart(buyerId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items
     * Add a product to the cart with validations (MOQ, active, approved, stock) and bulk price calculation.
     */
    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestBody CartItemRequest request) {
        try {
            CartResponse response = cartService.addItemToCart(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/cart/items/{id}
     * Update quantity of an item in the cart. Recalculates tier pricing and taxes.
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long id,
            @RequestBody UpdateCartItemRequest request) {
        try {
            CartResponse response = cartService.updateCartItemQuantity(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/cart/items/{id}
     * Remove a single item from the cart.
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long id) {
        try {
            CartResponse response = cartService.removeCartItem(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/cart/clear
     * Empty all items in the buyer's cart.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(
            @RequestParam(required = false, defaultValue = "1") Long buyerId) {
        CartResponse response = cartService.clearCart(buyerId);
        return ResponseEntity.ok(response);
    }
}
