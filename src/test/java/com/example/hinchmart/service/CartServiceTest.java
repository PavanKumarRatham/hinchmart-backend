package com.example.hinchmart.service;

import com.example.hinchmart.dto.CartItemRequest;
import com.example.hinchmart.dto.CartResponse;
import com.example.hinchmart.entity.*;
import com.example.hinchmart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBulkPriceRepository productBulkPriceRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private CartService cartService;

    private Product steelProduct;
    private List<ProductBulkPrice> bulkTiers;

    @BeforeEach
    void setUp() {
        steelProduct = new Product();
        steelProduct.setId(102L);
        steelProduct.setProductName("TMT Steel");
        steelProduct.setSeller("SteelCorp Ltd");
        steelProduct.setSku("STEEL-TMT-500");
        steelProduct.setUnit("Tons");
        steelProduct.setMoq(2);
        steelProduct.setStock(50);
        steelProduct.setSellingPrice(61500.0);
        steelProduct.setGstPercentage(18.0);
        steelProduct.setApprovalStatus("APPROVED");

        // Tier 1: 1 - 4 Tons -> ₹61,500
        ProductBulkPrice tier1 = new ProductBulkPrice();
        tier1.setId(1L);
        tier1.setMinQuantity(1);
        tier1.setMaxQuantity(4);
        tier1.setPrice(61500.0);
        tier1.setProduct(steelProduct);

        // Tier 2: 5 - 9 Tons -> ₹60,800
        ProductBulkPrice tier2 = new ProductBulkPrice();
        tier2.setId(2L);
        tier2.setMinQuantity(5);
        tier2.setMaxQuantity(9);
        tier2.setPrice(60800.0);
        tier2.setProduct(steelProduct);

        // Tier 3: 10+ Tons -> ₹59,900
        ProductBulkPrice tier3 = new ProductBulkPrice();
        tier3.setId(3L);
        tier3.setMinQuantity(10);
        tier3.setMaxQuantity(null);
        tier3.setPrice(59900.0);
        tier3.setProduct(steelProduct);

        bulkTiers = Arrays.asList(tier1, tier2, tier3);
    }

    @Test
    @DisplayName("B2B Calculation: 5 Tons should select Tier 2 (₹60,800), Subtotal ₹304,000, GST 18% ₹54,720, Total ₹358,720")
    void testBulkPricingAndGstCalculationFor5Tons() {
        when(productBulkPriceRepository.findByProductIdOrderByMinQuantityAsc(102L)).thenReturn(bulkTiers);

        CartService.PriceCalculation calculation = cartService.calculateBulkPriceAndGst(steelProduct, 5);

        assertEquals(60800.0, calculation.unitPrice);
        assertEquals(304000.0, calculation.subtotal); // 5 * 60,800
        assertEquals(18.0, calculation.gstPercentage);
        assertEquals(54720.0, calculation.gstAmount); // 304,000 * 0.18
        assertEquals(358720.0, calculation.totalWithGst); // 304,000 + 54,720
    }

    @Test
    @DisplayName("Validation: Fails when requested quantity < MOQ")
    void testMoqValidationFailure() {
        steelProduct.setMoq(5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.validateProductConstraints(steelProduct, 3);
        });

        assertTrue(exception.getMessage().contains("less than the Minimum Order Quantity"));
    }

    @Test
    @DisplayName("Validation: Fails when requested quantity > available stock")
    void testStockValidationFailure() {
        steelProduct.setStock(10);
        when(inventoryRepository.findByProductId(102L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.validateProductConstraints(steelProduct, 15);
        });

        assertTrue(exception.getMessage().contains("exceeds available stock"));
    }

    @Test
    @DisplayName("Validation: Fails when product approval status is REJECTED")
    void testApprovalStatusValidationFailure() {
        steelProduct.setApprovalStatus("REJECTED");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.validateProductConstraints(steelProduct, 5);
        });

        assertTrue(exception.getMessage().contains("not approved for purchase"));
    }

    @Test
    @DisplayName("Add to Cart: End-to-end flow with automatic bulk calculation")
    void testAddItemToCartSuccess() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setBuyerId(10L);

        when(productRepository.findById(102L)).thenReturn(Optional.of(steelProduct));
        when(cartRepository.findByBuyerId(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 102L)).thenReturn(Optional.empty());
        when(productBulkPriceRepository.findByProductIdOrderByMinQuantityAsc(102L)).thenReturn(bulkTiers);
        when(inventoryRepository.findByProductId(102L)).thenReturn(Optional.empty());

        CartItem savedCartItem = new CartItem();
        savedCartItem.setId(101L);
        savedCartItem.setCart(cart);
        savedCartItem.setProduct(steelProduct);
        savedCartItem.setQuantity(5);
        savedCartItem.setUnitPrice(60800.0);
        savedCartItem.setSubtotal(304000.0);
        savedCartItem.setGstPercentage(18.0);

        when(cartItemRepository.findByCartId(1L)).thenReturn(Collections.singletonList(savedCartItem));

        CartItemRequest request = new CartItemRequest(102L, 5, 10L);
        CartResponse response = cartService.addItemToCart(request);

        assertNotNull(response);
        assertEquals(1L, response.getCartId());
        assertEquals(10L, response.getBuyerId());
        assertEquals(5, response.getTotalItems());
        assertEquals(304000.0, response.getSubtotalAmount());
        assertEquals(54720.0, response.getTotalGstAmount());
        assertEquals(358720.0, response.getGrandTotal());
        assertEquals(1, response.getItems().size());
        assertEquals(60800.0, response.getItems().get(0).getUnitPrice());
    }
}
