package com.hinchmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hinchmart.dto.request.LoginRequest;
import com.hinchmart.dto.request.RegisterRequest;
import com.hinchmart.dto.request.SendOtpRequest;
import com.hinchmart.dto.request.VerifyOtpRequest;
import com.hinchmart.entity.enums.OtpPurpose;
import com.hinchmart.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBuyerRegistrationAndLogin() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "buyer_" + uniqueId + "@test.com";
        String phone = "9" + (System.currentTimeMillis() % 1000000000L);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPhone(phone);
        registerRequest.setPassword("TestPass@123");
        registerRequest.setFullName("Test Buyer Company");
        registerRequest.setRole(Role.BUYER);
        registerRequest.setCompanyName("Test Buildcon");
        registerRequest.setGstin("27AABCT1234F1Z0");
        registerRequest.setBusinessType("Contractor");
        registerRequest.setCity("Mumbai");

        // 1. Register Buyer
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.role").value("BUYER"))
                .andExpect(jsonPath("$.data.user.buyerProfile.companyName").value("Test Buildcon"));

        // 2. Login with credentials
        LoginRequest loginRequest = new LoginRequest(email, "TestPass@123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    public void testSellerRegistration() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "seller_" + uniqueId + "@test.com";
        String phone = "9" + ((System.currentTimeMillis() + 7) % 1000000000L);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPhone(phone);
        registerRequest.setPassword("SellerPass@123");
        registerRequest.setFullName("Steel Traders Ltd");
        registerRequest.setRole(Role.SELLER);
        registerRequest.setCompanyName("Steel Traders Ltd");
        registerRequest.setGstin("27AAACS5678P1Z3");
        registerRequest.setPanNumber("AAACS5678P");
        registerRequest.setBusinessType("Authorized Distributor");
        registerRequest.setCity("Pune");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("SELLER"))
                .andExpect(jsonPath("$.data.user.sellerProfile.status").value("PENDING"));
    }

    @Test
    public void testSendAndVerifyOtp() throws Exception {
        // 1. Send OTP
        SendOtpRequest sendOtpRequest = new SendOtpRequest("9876543210", OtpPurpose.LOGIN);
        mockMvc.perform(post("/api/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Verify OTP and login (default test OTP code is 123456)
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest("9876543210", "123456", OtpPurpose.LOGIN);
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("buyer@demo.com"));
    }

    @Test
    public void testGetMeWithToken() throws Exception {
        // Login as demo buyer
        LoginRequest loginRequest = new LoginRequest("buyer@demo.com", "Buyer@123");
        String responseContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseContent).path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("buyer@demo.com"))
                .andExpect(jsonPath("$.data.buyerProfile.companyName").value("Apex Infra Projects Pvt Ltd"));
    }
}
