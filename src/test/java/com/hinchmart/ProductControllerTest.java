package com.hinchmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hinchmart.dto.request.BulkPriceTierDto;
import com.hinchmart.dto.request.LoginRequest;
import com.hinchmart.dto.request.ProductCreateRequest;
import com.hinchmart.entity.enums.ProductUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetCategoriesAndSubcategories() throws Exception {
        // 1. Get Categories
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$.data[*].name", hasItem("Steel Rods & Rebars")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Cement & Concrete")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Pipes & Fittings")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Electrical & Cables")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Power Tools & Machinery")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Tiles & Flooring")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Paints & Waterproofing")))
                .andExpect(jsonPath("$.data[*].name", hasItem("Safety Equipment")));

        // 2. Get Subcategories
        mockMvc.perform(get("/api/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    public void testGetProductsAndTataTisconDetails() throws Exception {
        // 1. Get Products with pagination & search
        mockMvc.perform(get("/api/products?query=TATA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].productName").value("TATA Tiscon 550D TMT Bar"))
                .andExpect(jsonPath("$.data.content[0].unit").value("Ton"))
                .andExpect(jsonPath("$.data.content[0].moq").value(1))
                .andExpect(jsonPath("$.data.content[0].gstRate").value(18.00))
                .andExpect(jsonPath("$.data.content[0].sellingPrice").value(61500.00))
                .andExpect(jsonPath("$.data.content[0].stock").value(25));

        // 2. Get Product by Slug with Bulk Pricing Tiers
        mockMvc.perform(get("/api/products/slug/tata-tiscon-550d-tmt-bar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("TATA Tiscon 550D TMT Bar"))
                .andExpect(jsonPath("$.data.bulkPrices", hasSize(4)))
                .andExpect(jsonPath("$.data.bulkPrices[0].minQuantity").value(1))
                .andExpect(jsonPath("$.data.bulkPrices[0].maxQuantity").value(4))
                .andExpect(jsonPath("$.data.bulkPrices[0].pricePerUnit").value(61500.00))
                .andExpect(jsonPath("$.data.bulkPrices[1].minQuantity").value(5))
                .andExpect(jsonPath("$.data.bulkPrices[1].maxQuantity").value(9))
                .andExpect(jsonPath("$.data.bulkPrices[1].pricePerUnit").value(60800.00))
                .andExpect(jsonPath("$.data.bulkPrices[2].minQuantity").value(10))
                .andExpect(jsonPath("$.data.bulkPrices[2].maxQuantity").value(24))
                .andExpect(jsonPath("$.data.bulkPrices[2].pricePerUnit").value(59900.00))
                .andExpect(jsonPath("$.data.bulkPrices[3].minQuantity").value(25))
                .andExpect(jsonPath("$.data.bulkPrices[3].maxQuantity").doesNotExist())
                .andExpect(jsonPath("$.data.bulkPrices[3].pricePerUnit").value(58500.00));
    }

    @Test
    public void testSellerCreatesProductWithBulkPrices() throws Exception {
        // 1. Login as Seller
        LoginRequest loginRequest = new LoginRequest("seller@tata.com", "Seller@123");
        String responseContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseContent).path("data").path("accessToken").asText();

        // 2. Create Product
        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductName("JSW Neosteel 550D TMT Bar");
        request.setCategoryId(1L);
        request.setSku("JSW-TEST-" + System.currentTimeMillis());
        request.setHsnCode("72142090");
        request.setGstRate(new BigDecimal("18.00"));
        request.setMoq(2);
        request.setUnit(ProductUnit.TON);
        request.setMrp(new BigDecimal("64000.00"));
        request.setSellingPrice(new BigDecimal("60500.00"));
        request.setStock(50);
        request.setDeliveryDays(2);
        request.setDescription("JSW Neosteel Fe 550D TMT Rebars for heavy commercial structures.");

        BulkPriceTierDto tier1 = new BulkPriceTierDto(2, 9, new BigDecimal("60500.00"), BigDecimal.ZERO);
        BulkPriceTierDto tier2 = new BulkPriceTierDto(10, null, new BigDecimal("59000.00"), new BigDecimal("2.48"));
        request.setBulkPrices(List.of(tier1, tier2));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("JSW Neosteel 550D TMT Bar"))
                .andExpect(jsonPath("$.data.bulkPrices", hasSize(2)));
    }
}
