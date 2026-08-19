package com.hinchmart.service;

import com.hinchmart.dto.request.AddToCartRequest;
import com.hinchmart.dto.request.UpdateCartItemRequest;
import com.hinchmart.dto.response.CartDto;
import com.hinchmart.dto.response.CartItemDto;
import com.hinchmart.entity.*;
import com.hinchmart.entity.enums.AccountStatus;
import com.hinchmart.entity.enums.ApprovalStatus;
import com.hinchmart.entity.enums.SellerStatus;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.repository.CartItemRepository;
import com.hinchmart.repository.CartRepository;
import com.hinchmart.repository.ProductRepository;
import com.hinchmart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });
    }

    @Transactional(readOnly = true)
    public CartDto getCart(Long buyerUserId) {
        Cart cart = cartRepository.findByUserId(buyerUserId).orElse(null);
        if (cart == null) {
            CartDto empty = new CartDto();
            empty.setBuyerId(buyerUserId);
            return empty;
        }
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(Long buyerUserId, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        Cart cart = getOrCreateCart(buyerUserId);
        Optional<CartItem> existingOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        int totalQuantity = request.getQuantity();
        if (existingOpt.isPresent()) {
            totalQuantity += existingOpt.get().getQuantity();
        }

        // Validate MOQ
        validateProductForCart(product, totalQuantity);

        BigDecimal unitPrice = calculateBulkUnitPrice(product, totalQuantity);
        BigDecimal gstRate = product.getGstRate() != null ? product.getGstRate() : new BigDecimal("18.00");
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(totalQuantity)).setScale(2, RoundingMode.HALF_UP);

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(totalQuantity);
            existing.setUnitPrice(unitPrice);
            existing.setGstPercentage(gstRate);
            existing.setSubtotal(subtotal);
            cartItemRepository.save(existing);
        } else {
            CartItem newItem = new CartItem(
                    cart,
                    product,
                    product.getSeller(),
                    totalQuantity,
                    unitPrice,
                    gstRate,
                    subtotal
            );
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto updateCartItem(Long buyerUserId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(buyerUserId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        Product product = item.getProduct();
        validateProductForCart(product, request.getQuantity());

        BigDecimal unitPrice = calculateBulkUnitPrice(product, request.getQuantity());
        BigDecimal gstRate = product.getGstRate() != null ? product.getGstRate() : new BigDecimal("18.00");
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())).setScale(2, RoundingMode.HALF_UP);

        item.setQuantity(request.getQuantity());
        item.setUnitPrice(unitPrice);
        item.setGstPercentage(gstRate);
        item.setSubtotal(subtotal);
        cartItemRepository.save(item);

        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto removeCartItem(Long buyerUserId, Long cartItemId) {
        Cart cart = getOrCreateCart(buyerUserId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);

        return mapToCartDto(cart);
    }

    @Transactional
    public void clearCart(Long buyerUserId) {
        Cart cart = cartRepository.findByUserId(buyerUserId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.clearItems();
            cartRepository.save(cart);
        }
    }

    public void validateProductForCart(Product product, int quantity) {
        // 1. Quantity >= MOQ
        if (product.getMoq() != null && quantity < product.getMoq()) {
            throw new BadRequestException("Minimum Order Quantity for " + product.getProductName() + " is " + product.getMoq());
        }

        // 2. Product Active & Approved
        if (!product.isActive() || product.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BadRequestException("Product '" + product.getProductName() + "' is not approved or is currently inactive");
        }

        // 3. Seller Active & Approved
        User seller = product.getSeller();
        if (seller == null || seller.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Seller for this product is not active");
        }
        SellerProfile sp = seller.getSellerProfile();
        if (sp == null || sp.getStatus() != SellerStatus.APPROVED) {
            throw new BadRequestException("Seller for this product is pending verification");
        }

        // 4. Stock Available
        if (product.getStock() != null && product.getStock() < quantity) {
            throw new BadRequestException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ")");
        }
    }

    public BigDecimal calculateBulkUnitPrice(Product product, int quantity) {
        if (product.getBulkPrices() != null && !product.getBulkPrices().isEmpty()) {
            for (ProductBulkPrice bp : product.getBulkPrices()) {
                boolean minMatch = bp.getMinQuantity() == null || quantity >= bp.getMinQuantity();
                boolean maxMatch = bp.getMaxQuantity() == null || quantity <= bp.getMaxQuantity();
                if (minMatch && maxMatch) {
                    return bp.getPricePerUnit();
                }
            }
        }
        return product.getSellingPrice() != null ? product.getSellingPrice() : product.getMrp();
    }

    public CartDto mapToCartDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        if (cart.getUser() != null) {
            dto.setBuyerId(cart.getUser().getId());
        }

        List<CartItemDto> itemDtos = new ArrayList<>();
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        int totalItemsCount = 0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                Product p = item.getProduct();
                BigDecimal unitPrice = calculateBulkUnitPrice(p, item.getQuantity());
                BigDecimal regularPrice = p.getSellingPrice() != null ? p.getSellingPrice() : p.getMrp();
                boolean isBulkApplied = unitPrice.compareTo(regularPrice) < 0;

                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal gstRate = item.getGstPercentage() != null ? item.getGstPercentage() : new BigDecimal("18.00");
                BigDecimal gstAmount = subtotal.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal total = subtotal.add(gstAmount);

                CartItemDto itemDto = new CartItemDto();
                itemDto.setId(item.getId());
                itemDto.setProductId(p.getId());
                itemDto.setProductName(p.getProductName());
                itemDto.setProductSku(p.getSku());
                itemDto.setProductSlug(p.getSlug());
                itemDto.setUnit(p.getUnit());
                itemDto.setMoq(p.getMoq());
                itemDto.setAvailableStock(p.getStock());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setRegularPrice(regularPrice);
                itemDto.setUnitPrice(unitPrice);
                itemDto.setBulkTierApplied(isBulkApplied);
                itemDto.setGstPercentage(gstRate);
                itemDto.setGstAmount(gstAmount);
                itemDto.setSubtotal(subtotal);
                itemDto.setTotal(total);

                if (item.getSeller() != null) {
                    itemDto.setSellerId(item.getSeller().getId());
                    itemDto.setSellerName(item.getSeller().getFullName());
                    if (item.getSeller().getSellerProfile() != null) {
                        itemDto.setSellerCompanyName(item.getSeller().getSellerProfile().getCompanyName());
                    }
                }

                if (p.getProductImages() != null && !p.getProductImages().isEmpty()) {
                    itemDto.setPrimaryImageUrl(p.getProductImages().get(0).getImageUrl());
                }

                itemDtos.add(itemDto);
                totalSubtotal = totalSubtotal.add(subtotal);
                totalGst = totalGst.add(gstAmount);
                totalItemsCount += item.getQuantity();
            }
        }

        dto.setItems(itemDtos);
        dto.setTotalItems(totalItemsCount);
        dto.setSubtotal(totalSubtotal);
        dto.setGstTotal(totalGst);

        // Standard Delivery Charge for B2B orders: e.g. ₹2,500 for bulk transport or 0 if empty
        BigDecimal deliveryCharge = itemDtos.isEmpty() ? BigDecimal.ZERO : new BigDecimal("2500.00");
        dto.setDeliveryCharge(deliveryCharge);
        dto.setGrandTotal(totalSubtotal.add(totalGst).add(deliveryCharge));

        return dto;
    }
}
