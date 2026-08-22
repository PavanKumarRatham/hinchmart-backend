package com.example.hinchmart.controller;

import com.example.hinchmart.dto.RfqQuoteResponse;
import com.example.hinchmart.service.RfqQuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyer")
@CrossOrigin
public class BuyerRfqController {

    private final RfqQuoteService rfqQuoteService;

    public BuyerRfqController(RfqQuoteService rfqQuoteService) {
        this.rfqQuoteService = rfqQuoteService;
    }

    /**
     * GET /api/buyer/rfqs/{id}/quotes
     * Buyer views and compares all seller quotes for RFQ {id}.
     */
    @GetMapping("/rfqs/{id}/quotes")
    public ResponseEntity<?> getQuotesForRfq(@PathVariable Long id) {
        try {
            List<RfqQuoteResponse> quotes = rfqQuoteService.getQuotesForBuyerRfq(id);
            return ResponseEntity.ok(quotes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/buyer/quotes/{id}/accept
     * Buyer accepts a specific seller quotation (marks selected quote ACCEPTED and competing quotes REJECTED).
     */
    @PostMapping("/quotes/{id}/accept")
    public ResponseEntity<?> acceptQuote(@PathVariable Long id) {
        try {
            RfqQuoteResponse response = rfqQuoteService.acceptQuote(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/buyer/quotes/{id}/reject
     * Buyer rejects a specific seller quotation.
     */
    @PostMapping("/quotes/{id}/reject")
    public ResponseEntity<?> rejectQuote(@PathVariable Long id) {
        try {
            RfqQuoteResponse response = rfqQuoteService.rejectQuote(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
