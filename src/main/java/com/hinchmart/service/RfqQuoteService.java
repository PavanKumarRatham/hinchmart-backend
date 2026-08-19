package com.hinchmart.service;

import com.hinchmart.dto.request.CreateRfqQuoteRequest;
import com.hinchmart.dto.response.RfqDto;
import com.hinchmart.dto.response.RfqQuoteDto;
import com.hinchmart.entity.Rfq;
import com.hinchmart.entity.RfqQuote;
import com.hinchmart.entity.SellerProfile;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.QuoteStatus;
import com.hinchmart.entity.enums.RfqStatus;
import com.hinchmart.entity.enums.SellerStatus;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.RfqQuoteRepository;
import com.hinchmart.repository.RfqRepository;
import com.hinchmart.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RfqQuoteService {

    private final RfqQuoteRepository rfqQuoteRepository;
    private final RfqRepository rfqRepository;
    private final UserRepository userRepository;
    private final RfqService rfqService;
    private final ActivityLogService activityLogService;

    public RfqQuoteService(RfqQuoteRepository rfqQuoteRepository,
                           RfqRepository rfqRepository,
                           UserRepository userRepository,
                           RfqService rfqService,
                           ActivityLogService activityLogService) {
        this.rfqQuoteRepository = rfqQuoteRepository;
        this.rfqRepository = rfqRepository;
        this.userRepository = userRepository;
        this.rfqService = rfqService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public Page<RfqDto> getSellerRfqs(Pageable pageable) {
        return rfqService.getAllRfqs(pageable);
    }

    @Transactional(readOnly = true)
    public RfqDto getSellerRfqById(Long rfqId, Long sellerUserId) {
        return rfqService.getRfqById(rfqId, sellerUserId);
    }

    @Transactional
    public RfqQuoteDto submitQuote(Long sellerUserId, Long rfqId, CreateRfqQuoteRequest request) {
        User seller = userRepository.findById(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerUserId));

        SellerProfile sp = seller.getSellerProfile();
        if (sp == null || sp.getStatus() != SellerStatus.APPROVED) {
            throw new BadRequestException("Seller verification is pending. Only approved sellers can quote on RFQs.");
        }

        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found with ID: " + rfqId));

        if (rfq.getStatus() != RfqStatus.OPEN && rfq.getStatus() != RfqStatus.IN_REVIEW) {
            throw new BadRequestException("This RFQ is no longer open for quotations.");
        }

        Optional<RfqQuote> existingQuoteOpt = rfqQuoteRepository.findByRfqIdAndSellerId(rfqId, sellerUserId);

        RfqQuote quote;
        if (existingQuoteOpt.isPresent()) {
            quote = existingQuoteOpt.get();
            quote.setPrice(request.getPrice());
            quote.setGstPercentage(request.getGstPercentage() != null ? request.getGstPercentage() : new BigDecimal("18.00"));
            quote.setDeliveryCharge(request.getDeliveryCharge() != null ? request.getDeliveryCharge() : BigDecimal.ZERO);
            quote.setDeliveryDays(request.getDeliveryDays());
            quote.setValidUntil(request.getValidUntil() != null ? request.getValidUntil() : LocalDateTime.now().plusDays(7));
            quote.setPaymentTerms(request.getPaymentTerms());
            quote.setRemarks(request.getRemarks());
            quote.setStatus(QuoteStatus.SUBMITTED);
        } else {
            quote = new RfqQuote(
                    rfq,
                    seller,
                    request.getPrice(),
                    request.getGstPercentage(),
                    request.getDeliveryCharge(),
                    request.getDeliveryDays(),
                    request.getValidUntil() != null ? request.getValidUntil() : LocalDateTime.now().plusDays(7),
                    request.getPaymentTerms(),
                    request.getRemarks(),
                    QuoteStatus.SUBMITTED
            );
        }

        RfqQuote saved = rfqQuoteRepository.save(quote);

        activityLogService.log(sellerUserId, seller.getEmail(), "RFQ_QUOTE_SUBMITTED", "RFQ_QUOTE",
                saved.getId(), "Submitted quote of ₹" + saved.getPrice() + " for RFQ " + rfq.getRfqNumber(), null);

        return mapToQuoteDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RfqQuoteDto> getBuyerQuotesForRfq(Long buyerUserId, Long rfqId) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found: " + rfqId));

        if (!rfq.getBuyer().getId().equals(buyerUserId)) {
            throw new UnauthorizedException("You do not have permission to view quotes for this RFQ");
        }

        return rfqQuoteRepository.findByRfqIdOrderByCreatedAtDesc(rfqId).stream()
                .map(this::mapToQuoteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RfqQuoteDto acceptQuote(Long buyerUserId, Long quoteId) {
        RfqQuote quote = rfqQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + quoteId));

        Rfq rfq = quote.getRfq();
        if (!rfq.getBuyer().getId().equals(buyerUserId)) {
            throw new UnauthorizedException("You do not have permission to accept this quote");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        RfqQuote accepted = rfqQuoteRepository.save(quote);

        // Reject other quotes for the same RFQ
        List<RfqQuote> otherQuotes = rfqQuoteRepository.findByRfqIdOrderByCreatedAtDesc(rfq.getId());
        for (RfqQuote q : otherQuotes) {
            if (!q.getId().equals(quoteId)) {
                q.setStatus(QuoteStatus.REJECTED);
                rfqQuoteRepository.save(q);
            }
        }

        rfq.setStatus(RfqStatus.CLOSED);
        rfqRepository.save(rfq);

        activityLogService.log(buyerUserId, rfq.getBuyer().getEmail(), "RFQ_QUOTE_ACCEPTED", "RFQ_QUOTE",
                quoteId, "Accepted quote from " + quote.getSeller().getFullName() + " for RFQ " + rfq.getRfqNumber(), null);

        return mapToQuoteDto(accepted);
    }

    @Transactional
    public RfqQuoteDto rejectQuote(Long buyerUserId, Long quoteId) {
        RfqQuote quote = rfqQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + quoteId));

        Rfq rfq = quote.getRfq();
        if (!rfq.getBuyer().getId().equals(buyerUserId)) {
            throw new UnauthorizedException("You do not have permission to reject this quote");
        }

        quote.setStatus(QuoteStatus.REJECTED);
        RfqQuote rejected = rfqQuoteRepository.save(quote);

        activityLogService.log(buyerUserId, rfq.getBuyer().getEmail(), "RFQ_QUOTE_REJECTED", "RFQ_QUOTE",
                quoteId, "Rejected quote from " + quote.getSeller().getFullName() + " for RFQ " + rfq.getRfqNumber(), null);

        return mapToQuoteDto(rejected);
    }

    public RfqQuoteDto mapToQuoteDto(RfqQuote quote) {
        RfqQuoteDto dto = new RfqQuoteDto();
        dto.setId(quote.getId());
        dto.setPrice(quote.getPrice());
        dto.setGstPercentage(quote.getGstPercentage());
        dto.setDeliveryCharge(quote.getDeliveryCharge());
        dto.setDeliveryDays(quote.getDeliveryDays());
        dto.setValidUntil(quote.getValidUntil());
        dto.setPaymentTerms(quote.getPaymentTerms());
        dto.setRemarks(quote.getRemarks());
        dto.setStatus(quote.getStatus());
        dto.setCreatedAt(quote.getCreatedAt());
        dto.setUpdatedAt(quote.getUpdatedAt());

        if (quote.getRfq() != null) {
            dto.setRfqId(quote.getRfq().getId());
            dto.setRfqNumber(quote.getRfq().getRfqNumber());
            dto.setRfqTitle(quote.getRfq().getTitle());
        }

        if (quote.getSeller() != null) {
            dto.setSellerId(quote.getSeller().getId());
            dto.setSellerName(quote.getSeller().getFullName());
            if (quote.getSeller().getSellerProfile() != null) {
                dto.setSellerCompanyName(quote.getSeller().getSellerProfile().getCompanyName());
            }
        }

        return dto;
    }
}
