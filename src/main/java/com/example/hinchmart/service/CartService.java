package com.example.hinchmart.service;

import com.example.hinchmart.dto.*;
import com.example.hinchmart.entity.*;
import com.example.hinchmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductBulkPriceRepository productBulkPriceRepository;
    private final InventoryRepository inventoryRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       ProductBulkPriceRepository productBulkPriceRepository,
                       InventoryRepository inventoryRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productBulkPriceRepository = productBulkPriceRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Get or create a cart for the specified buyer.
     */
    @Transactional
    public Cart getOrCreateCartEntity(Long buyerId) {
        return cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setBuyerId(buyerId);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Retrieve the buyer's cart with up-to-date bulk pricing and GST calculations.
     */
    @Transactional
    public CartResponse getCart(Long buyerId) {
        Cart cart = getOrCreateCartEntity(buyerId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = new ArrayList<>();
        double totalSubtotal = 0.0;
        double totalGst = 0.0;
        int totalQuantity = 0;

        for (CartItem item : items) {
            Product product = item.getProduct();

            // Recalculate price dynamically to reflect current bulk tiers
            PriceCalculation calculation = calculateBulkPriceAndGst(product, item.getQuantity());

            // Synchronize entity if prices changed
            item.setUnitPrice(calculation.unitPrice);
            item.setSubtotal(calculation.subtotal);
            item.setGstPercentage(calculation.gstPercentage);
            cartItemRepository.save(item);

            CartItemResponse itemResp = new CartItemResponse();
            itemResp.setId(item.getId());
            itemResp.setProductId(product.getId());
            itemResp.setProductName(product.getProductName());
            itemResp.setSku(product.getSku());
            itemResp.setSeller(product.getSeller());
            itemResp.setSellerId(item.getSellerId());
            itemResp.setUnit(product.getUnit());
            itemResp.setQuantity(item.getQuantity());
            itemResp.setMoq(product.getMoq() != null ? product.getMoq() : 1);
            itemResp.setUnitPrice(round(calculation.unitPrice));
            itemResp.setSubtotal(round(calculation.subtotal));
            itemResp.setGstPercentage(calculation.gstPercentage);
            itemResp.setGstAmount(round(calculation.gstAmount));
            itemResp.setTotalPrice(round(calculation.totalWithGst));
            itemResp.setAppliedTierInfo(calculation.tierInfo);

            itemResponses.add(itemResp);

            totalSubtotal += calculation.subtotal;
            totalGst += calculation.gstAmount;
            totalQuantity += item.getQuantity();
        }

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setBuyerId(buyerId);
        response.setTotalItems(totalQuantity);
        response.setTotalLineItems(itemResponses.size());
        response.setSubtotalAmount(round(totalSubtotal));
        response.setTotalGstAmount(round(totalGst));
        response.setGrandTotal(round(totalSubtotal + totalGst));
        response.setCurrency("INR");
        response.setItems(itemResponses);

        return response;
    }

    /**
     * Add an item to the buyer's cart with all B2B business validations.
     */
    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        Long buyerId = request.getBuyerId() != null ? request.getBuyerId() : 1L;
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        Cart cart = getOrCreateCartEntity(buyerId);

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        int targetQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        // Validate MOQ, Stock, Active status, and Seller
        validateProductConstraints(product, targetQuantity);

        // Calculate unit price using bulk pricing tier rules & GST
        PriceCalculation calculation = calculateBulkPriceAndGst(product, targetQuantity);

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            cartItem.setQuantity(targetQuantity);
            cartItem.setUnitPrice(calculation.unitPrice);
            cartItem.setSubtotal(calculation.subtotal);
            cartItem.setGstPercentage(calculation.gstPercentage);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setSeller(product.getSeller());
            cartItem.setQuantity(targetQuantity);
            cartItem.setUnitPrice(calculation.unitPrice);
            cartItem.setSubtotal(calculation.subtotal);
            cartItem.setGstPercentage(calculation.gstPercentage);
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        cartItemRepository.save(cartItem);

        return getCart(buyerId);
    }

    /**
     * Update quantity of an existing cart item.
     */
    @Transactional
    public CartResponse updateCartItemQuantity(Long cartItemId, UpdateCartItemRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0. To remove item, use DELETE /api/cart/items/" + cartItemId);
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        Product product = cartItem.getProduct();
        int newQuantity = request.getQuantity();

        // Validate constraints
        validateProductConstraints(product, newQuantity);

        // Recalculate price using bulk tiers for the new quantity
        PriceCalculation calculation = calculateBulkPriceAndGst(product, newQuantity);

        cartItem.setQuantity(newQuantity);
        cartItem.setUnitPrice(calculation.unitPrice);
        cartItem.setSubtotal(calculation.subtotal);
        cartItem.setGstPercentage(calculation.gstPercentage);
        cartItem.setUpdatedAt(LocalDateTime.now());

        cartItemRepository.save(cartItem);

        return getCart(cartItem.getCart().getBuyerId());
    }

    /**
     * Remove a single item from the cart.
     */
    @Transactional
    public CartResponse removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        Long buyerId = cartItem.getCart().getBuyerId();
        cartItemRepository.delete(cartItem);

        return getCart(buyerId);
    }

    /**
     * Clear all items in the cart for a buyer.
     */
    @Transactional
    public CartResponse clearCart(Long buyerId) {
        Cart cart = getOrCreateCartEntity(buyerId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        cartItemRepository.deleteAll(items);

        return getCart(buyerId);
    }

    /**
     * B2B Validation Engine:
     * 1. Quantity >= MOQ
     * 2. Seller Active / Present
     * 3. Product Active
     * 4. Product Approved
     * 5. Stock Available
     */
    public void validateProductConstraints(Product product, int requestedQuantity) {
        // 1. Quantity >= MOQ
        int moq = (product.getMoq() != null && product.getMoq() > 0) ? product.getMoq() : 1;
        if (requestedQuantity < moq) {
            throw new IllegalArgumentException(
                    "Requested quantity (" + requestedQuantity + " " + (product.getUnit() != null ? product.getUnit() : "units") +
                            ") is less than the Minimum Order Quantity (MOQ: " + moq + " " +
                            (product.getUnit() != null ? product.getUnit() : "units") + ") for product: '" + product.getProductName() + "'"
            );
        }

        // 2. Seller Active / Valid
        if (product.getSeller() == null || product.getSeller().trim().isEmpty()) {
            throw new IllegalArgumentException("Product '" + product.getProductName() + "' does not have an active seller.");
        }

        // 3. Product Active
        if (product.getCategory() != null && "false".equalsIgnoreCase(product.getCategory().getActive())) {
            throw new IllegalArgumentException("Product category '" + product.getCategory().getName() + "' is currently inactive.");
        }

        // 4. Product Approved
        if (product.getApprovalStatus() != null) {
            String status = product.getApprovalStatus().trim().toUpperCase();
            if ("REJECTED".equals(status) || "PENDING".equals(status) || "INACTIVE".equals(status)) {
                throw new IllegalArgumentException(
                        "Product '" + product.getProductName() + "' is not approved for purchase. Status: " + product.getApprovalStatus()
                );
            }
        }

        // 5. Stock Available (Check Inventory table first, then Product.stock)
        int availableStock = 0;
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(product.getId());
        if (inventoryOpt.isPresent() && inventoryOpt.get().getAvailableStock() != null) {
            availableStock = inventoryOpt.get().getAvailableStock();
        } else if (product.getStock() != null) {
            availableStock = product.getStock();
        } else {
            availableStock = Integer.MAX_VALUE; // If stock not managed, treat as unconstrained
        }

        if (requestedQuantity > availableStock) {
            throw new IllegalArgumentException(
                    "Requested quantity (" + requestedQuantity + ") exceeds available stock (" + availableStock + ") for product: '" + product.getProductName() + "'"
            );
        }
    }

    /**
     * B2B Dynamic Bulk Pricing & GST Calculation Engine:
     * - Searches ProductBulkPrice tiers for product.
     * - Matches matching tier based on quantity (e.g. 1-4 tons: 61,500, 5-9 tons: 60,800, 10+: 59,900).
     * - Calculates Subtotal = quantity * unitPrice.
     * - Calculates GST = Subtotal * (gstPercentage / 100).
     * - Calculates Total = Subtotal + GST.
     */
    public PriceCalculation calculateBulkPriceAndGst(Product product, int quantity) {
        List<ProductBulkPrice> bulkTiers = productBulkPriceRepository.findByProductIdOrderByMinQuantityAsc(product.getId());

        Double selectedUnitPrice = null;
        String tierInfo = null;

        if (bulkTiers != null && !bulkTiers.isEmpty()) {
            for (ProductBulkPrice tier : bulkTiers) {
                int min = tier.getMinQuantity() != null ? tier.getMinQuantity() : 1;
                int max = tier.getMaxQuantity() != null ? tier.getMaxQuantity() : Integer.MAX_VALUE;

                if (quantity >= min && quantity <= max) {
                    selectedUnitPrice = tier.getPrice();
                    tierInfo = "Bulk Tier (" + min + (max == Integer.MAX_VALUE ? "+ " : "-" + max + " ") +
                            (product.getUnit() != null ? product.getUnit() : "units") + "): ₹" + String.format("%,.2f", selectedUnitPrice);
                    break;
                }
            }

            // If quantity exceeds all defined max ranges, apply the highest tier's price
            if (selectedUnitPrice == null) {
                ProductBulkPrice highestTier = bulkTiers.get(bulkTiers.size() - 1);
                selectedUnitPrice = highestTier.getPrice();
                tierInfo = "Bulk Tier (" + highestTier.getMinQuantity() + "+ " +
                        (product.getUnit() != null ? product.getUnit() : "units") + "): ₹" + String.format("%,.2f", selectedUnitPrice);
            }
        }

        // Fallback to standard product selling price or MRP if bulk tiers not configured
        if (selectedUnitPrice == null || selectedUnitPrice <= 0) {
            if (product.getSellingPrice() != null && product.getSellingPrice() > 0) {
                selectedUnitPrice = product.getSellingPrice();
            } else if (product.getMrp() != null && product.getMrp() > 0) {
                selectedUnitPrice = product.getMrp();
            } else {
                selectedUnitPrice = 0.0;
            }
            tierInfo = "Standard Price: ₹" + String.format("%,.2f", selectedUnitPrice);
        }

        double subtotal = quantity * selectedUnitPrice;
        double gstPercentage = product.getGstPercentage() != null ? product.getGstPercentage() : 0.0;
        double gstAmount = (subtotal * gstPercentage) / 100.0;
        double totalWithGst = subtotal + gstAmount;

        return new PriceCalculation(selectedUnitPrice, subtotal, gstPercentage, gstAmount, totalWithGst, tierInfo);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Inner helper record/class for calculated price breakdown
     */
    public static class PriceCalculation {
        public final double unitPrice;
        public final double subtotal;
        public final double gstPercentage;
        public final double gstAmount;
        public final double totalWithGst;
        public final String tierInfo;

        public PriceCalculation(double unitPrice, double subtotal, double gstPercentage, double gstAmount, double totalWithGst, String tierInfo) {
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
            this.gstPercentage = gstPercentage;
            this.gstAmount = gstAmount;
            this.totalWithGst = totalWithGst;
            this.tierInfo = tierInfo;
        }
    }
}
