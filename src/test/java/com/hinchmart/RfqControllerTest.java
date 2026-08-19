package com.hinchmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hinchmart.dto.request.LoginRequest;
import com.hinchmart.dto.request.RfqCreateRequest;
import com.hinchmart.dto.request.RfqItemRequest;
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
public class RfqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBuyerSubmitsAndRetrievesRfq() throws Exception {
        // 1. Login as Buyer
        LoginRequest loginRequest = new LoginRequest("buyer@demo.com", "Buyer@123");
        String responseContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseContent).path("data").path("accessToken").asText();

        // 2. Submit new RFQ
        RfqCreateRequest rfqRequest = new RfqCreateRequest();
        rfqRequest.setTitle("Urgent Requirement of 100 Tons Steel for Flyover Pier #12");
        rfqRequest.setNotes("Delivery required at Baner Pune within 4 days. Unloading by buyer.");
        rfqRequest.setDeliveryPincode("411045");
        rfqRequest.setDeliveryTimelineDays(4);

        RfqItemRequest item = new RfqItemRequest();
        item.setProductName("TATA Tiscon 550D TMT Bar (20mm)");
        item.setQuantity(100);
        item.setUnit(ProductUnit.TON);
        item.setTargetPrice(new BigDecimal("57500.00"));
        item.setSpecifications("Standard 12m length bundles with manufacturer test certificate");
        rfqRequest.setItems(List.of(item));

        String rfqResponse = mockMvc.perform(post("/api/rfqs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rfqRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rfqNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value("Urgent Requirement of 100 Tons Steel for Flyover Pier #12"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].quantity").value(100))
                .andExpect(jsonPath("$.data.items[0].unit").value("Ton"))
                .andReturn().getResponse().getContentAsString();

        Long rfqId = objectMapper.readTree(rfqResponse).path("data").path("id").asLong();

        // 3. Get My RFQs
        mockMvc.perform(get("/api/rfqs/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        // 4. Get RFQ by ID
        mockMvc.perform(get("/api/rfqs/" + rfqId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(rfqId))
                .andExpect(jsonPath("$.data.buyerEmail").value("buyer@demo.com"));
    }
}
