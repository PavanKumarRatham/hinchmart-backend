package com.example.hinchmart.service;

import com.example.hinchmart.dto.RfqQuoteRequest;
import com.example.hinchmart.dto.RfqQuoteResponse;
import com.example.hinchmart.entity.Rfq;
import com.example.hinchmart.entity.RfqItem;
import com.example.hinchmart.entity.RfqQuote;
import com.example.hinchmart.repository.RfqQuoteRepository;
import com.example.hinchmart.repository.RfqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RfqQuoteService {

    private final RfqQuoteRepository rfqQuoteRepository;
    private final RfqRepository rfqRepository;
    private final NotificationService notificationService;

    public RfqQuoteService(RfqQuoteRepository rfqQuoteRepository,
                           RfqRepository rfqRepository,
                           NotificationService notificationService) {
        this.rfqQuoteRepository = rfqQuoteRepository;
        this.rfqRepository = rfqRepository;
        this.notificationService = notificationService;
    }

    /**
     * GET /api/seller/rfqs
     * Sellers view all open RFQs submitted by buyers.
     */
    @Transactional(readOnly = true)
    public List<Rfq> getOpenRfqsForSellers() {
        return rfqRepository.findAll();
    }

    /**
     * GET /api/seller/rfqs/{id}
     * Seller views details of a specific RFQ to prepare quotation.
     */
    @Transactional(readOnly = true)
    public Rfq getRfqDetails(Long rfqId) {
        return rfqRepository.findById(rfqId)
                .orElseThrow(() -> new RuntimeException("RFQ not found with id: " + rfqId));
    }

    /**
     * POST /api/seller/rfqs/{id}/quote
     * Seller submits a competitive quotation for RFQ {id}.
     */
    @Transactional
    public RfqQuoteResponse submitQuote(Long rfqId, RfqQuoteRequest request) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new RuntimeException("RFQ not found with id: " + rfqId));

        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("Quoted price must be greater than 0");
        }
        if (request.getSellerId() == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }

        // Check if seller already submitted a quote for this RFQ
        Optional<RfqQuote> existingQuoteOpt = rfqQuoteRepository.findByRfqIdAndSellerId(rfqId, request.getSellerId());
        RfqQuote quote;
        if (existingQuoteOpt.isPresent()) {
            quote = existingQuoteOpt.get();
        } else {
            quote = new RfqQuote();
            quote.setRfq(rfq);
            quote.setSellerId(request.getSellerId());
            quote.setCreatedAt(LocalDateTime.now());
        }

        quote.setSellerName(request.getSellerName() != null ? request.getSellerName() : "Seller #" + request.getSellerId());
        quote.setPrice(round(request.getPrice()));
        quote.setGstPercentage(request.getGstPercentage() != null ? request.getGstPercentage() : 18.0);
        quote.setDeliveryCharge(request.getDeliveryCharge() != null ? round(request.getDeliveryCharge()) : 0.0);
        quote.setDeliveryDays(request.getDeliveryDays() != null ? request.getDeliveryDays() : 3);
        quote.setValidUntil(request.getValidUntil());
        quote.setPaymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : "100% Advance");
        quote.setRemarks(request.getRemarks());
        quote.setStatus("PENDING");
        quote.setUpdatedAt(LocalDateTime.now());

        RfqQuote saved = rfqQuoteRepository.save(quote);

        // Update RFQ status to QUOTED if still SUBMITTED
        if ("SUBMITTED".equalsIgnoreCase(rfq.getStatus())) {
            rfq.setStatus("QUOTED");
            rfqRepository.save(rfq);
        }

        // Notify Buyer of incoming quote
        try {
            notificationService.sendNotification(
                    rfq.getBuyerId(),
                    "BUYER",
                    "QUOTE_RECEIVED",
                    "New Quotation Received for RFQ #" + rfq.getId(),
                    quote.getSellerName() + " submitted a quote of ₹" + quote.getPrice() + "/unit with " + quote.getDeliveryDays() + " days delivery.",
                    "QUOTE",
                    saved.getId()
            );
        } catch (Exception ignored) {
        }

        return mapToQuoteResponse(saved, rfq);
    }

    /**
     * GET /api/buyer/rfqs/{id}/quotes
     * Buyer fetches all seller quotes for RFQ {id} to compare prices, delivery times, and terms.
     */
    @Transactional(readOnly = true)
    public List<RfqQuoteResponse> getQuotesForBuyerRfq(Long rfqId) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new RuntimeException("RFQ not found with id: " + rfqId));

        List<RfqQuote> quotes = rfqQuoteRepository.findByRfqId(rfqId);
        return quotes.stream().map(q -> mapToQuoteResponse(q, rfq)).collect(Collectors.toList());
    }

    /**
     * POST /api/buyer/quotes/{id}/accept
     * Buyer accepts a selected quote -> marks it ACCEPTED, rejects competing quotes, and updates RFQ status.
     */
    @Transactional
    public RfqQuoteResponse acceptQuote(Long quoteId) {
        RfqQuote acceptedQuote = rfqQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + quoteId));

        Rfq rfq = acceptedQuote.getRfq();

        // 1. Mark this quote as ACCEPTED
        acceptedQuote.setStatus("ACCEPTED");
        acceptedQuote.setUpdatedAt(LocalDateTime.now());
        rfqQuoteRepository.save(acceptedQuote);

        // 2. Reject all other competing quotes for this RFQ
        List<RfqQuote> allQuotes = rfqQuoteRepository.findByRfqId(rfq.getId());
        for (RfqQuote otherQuote : allQuotes) {
            if (!otherQuote.getId().equals(quoteId)) {
                otherQuote.setStatus("REJECTED");
                otherQuote.setUpdatedAt(LocalDateTime.now());
                rfqQuoteRepository.save(otherQuote);
            }
        }

        // 3. Update RFQ status
        rfq.setStatus("QUOTE_ACCEPTED");
        rfqRepository.save(rfq);

        // 4. Notify winning seller
        try {
            notificationService.sendNotification(
                    acceptedQuote.getSellerId(),
                    "SELLER",
                    "QUOTE_ACCEPTED",
                    "Quotation Accepted for RFQ #" + rfq.getId() + "!",
                    "Buyer has accepted your quotation. Prepare order fulfillment.",
                    "QUOTE",
                    acceptedQuote.getId()
            );
        } catch (Exception ignored) {
        }

        return mapToQuoteResponse(acceptedQuote, rfq);
    }

    /**
     * POST /api/buyer/quotes/{id}/reject
     * Buyer rejects a specific quote.
     */
    @Transactional
    public RfqQuoteResponse rejectQuote(Long quoteId) {
        RfqQuote quote = rfqQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + quoteId));

        quote.setStatus("REJECTED");
        quote.setUpdatedAt(LocalDateTime.now());
        RfqQuote updated = rfqQuoteRepository.save(quote);

        return mapToQuoteResponse(updated, quote.getRfq());
    }

    private RfqQuoteResponse mapToQuoteResponse(RfqQuote quote, Rfq rfq) {
        // Calculate total RFQ quantity across items
        int totalQty = 0;
        String unit = "units";
        if (rfq != null && rfq.getItems() != null && !rfq.getItems().isEmpty()) {
            for (RfqItem item : rfq.getItems()) {
                totalQty += (item.getQuantity() != null ? item.getQuantity() : 0);
                if (item.getUnit() != null) {
                    unit = item.getUnit();
                }
            }
        }
        if (totalQty == 0) totalQty = 1;

        double unitPrice = quote.getPrice() != null ? quote.getPrice() : 0.0;
        double subtotal = round(unitPrice * totalQty);
        double gstPercentage = quote.getGstPercentage() != null ? quote.getGstPercentage() : 18.0;
        double gstAmount = round((subtotal * gstPercentage) / 100.0);
        double deliveryCharge = quote.getDeliveryCharge() != null ? quote.getDeliveryCharge() : 0.0;
        double grandTotal = round(subtotal + gstAmount + deliveryCharge);

        RfqQuoteResponse resp = new RfqQuoteResponse();
        resp.setId(quote.getId());
        resp.setRfqId(rfq != null ? rfq.getId() : null);
        resp.setSellerId(quote.getSellerId());
        resp.setSellerName(quote.getSellerName());
        resp.setPrice(unitPrice);
        resp.setTotalQuantity(totalQty);
        resp.setUnit(unit);
        resp.setSubtotal(subtotal);
        resp.setGstPercentage(gstPercentage);
        resp.setGstAmount(gstAmount);
        resp.setDeliveryCharge(deliveryCharge);
        resp.setGrandTotal(grandTotal);
        resp.setDeliveryDays(quote.getDeliveryDays());
        resp.setValidUntil(quote.getValidUntil());
        resp.setPaymentTerms(quote.getPaymentTerms());
        resp.setRemarks(quote.getRemarks());
        resp.setStatus(quote.getStatus());
        resp.setCreatedAt(quote.getCreatedAt());

        return resp;
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
