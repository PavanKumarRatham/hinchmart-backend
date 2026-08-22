package com.example.hinchmart.controller;

import com.example.hinchmart.dto.RfqQuoteRequest;
import com.example.hinchmart.dto.RfqQuoteResponse;
import com.example.hinchmart.entity.Rfq;
import com.example.hinchmart.service.RfqQuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/rfqs")
@CrossOrigin
public class SellerRfqController {

    private final RfqQuoteService rfqQuoteService;

    public SellerRfqController(RfqQuoteService rfqQuoteService) {
        this.rfqQuoteService = rfqQuoteService;
    }

    /**
     * GET /api/seller/rfqs
     * Sellers view all RFQs open for quotation.
     */
    @GetMapping
    public ResponseEntity<List<Rfq>> getOpenRfqs() {
        List<Rfq> rfqs = rfqQuoteService.getOpenRfqsForSellers();
        return ResponseEntity.ok(rfqs);
    }

    /**
     * GET /api/seller/rfqs/{id}
     * Seller views details of a specific RFQ to prepare quotation.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRfqDetails(@PathVariable Long id) {
        try {
            Rfq rfq = rfqQuoteService.getRfqDetails(id);
            return ResponseEntity.ok(rfq);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/seller/rfqs/{id}/quote
     * Seller submits quotation for RFQ {id}.
     */
    @PostMapping("/{id}/quote")
    public ResponseEntity<?> submitQuote(
            @PathVariable Long id,
            @RequestBody RfqQuoteRequest request) {
        try {
            RfqQuoteResponse response = rfqQuoteService.submitQuote(id, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
