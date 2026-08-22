package com.example.hinchmart.service;


import com.example.hinchmart.dto.RfqItemRequest;
import com.example.hinchmart.dto.RfqRequest;
import com.example.hinchmart.entity.Product;
import com.example.hinchmart.entity.Rfq;
import com.example.hinchmart.entity.RfqItem;
import com.example.hinchmart.repository.ProductRepository;
import com.example.hinchmart.repository.RfqRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RfqService {

    private final RfqRepository rfqRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public RfqService(
            RfqRepository rfqRepository,
            ProductRepository productRepository,
            NotificationService notificationService) {

        this.rfqRepository = rfqRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public Rfq createRfq(RfqRequest request) {

        Rfq rfq = new Rfq();

        rfq.setBuyerId(request.getBuyerId());
        rfq.setDeliveryLocation(request.getDeliveryLocation());
        rfq.setRequiredDate(request.getRequiredDate());
        rfq.setStatus("SUBMITTED");
        rfq.setCreatedAt(LocalDateTime.now());

        for (RfqItemRequest itemRequest : request.getItems()) {

            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found"));

            RfqItem item = new RfqItem();

            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnit(itemRequest.getUnit());
            item.setRemarks(itemRequest.getRemarks());
            item.setRfq(rfq);

            rfq.getItems().add(item);
        }

        Rfq saved = rfqRepository.save(rfq);

        try {
            notificationService.sendNotification(
                    101L,
                    "SELLER",
                    "RFQ_RECEIVED",
                    "New RFQ Received #" + saved.getId(),
                    "A new RFQ has been submitted for " + saved.getDeliveryLocation() + " with " + saved.getItems().size() + " items.",
                    "RFQ",
                    saved.getId()
            );
        } catch (Exception ignored) {
        }

        return saved;
    }

    public List<Rfq> getMyRfqs(Long buyerId) {
        return rfqRepository.findByBuyerId(buyerId);
    }

    public Rfq getRfqById(Long id) {
        return rfqRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("RFQ not found"));
    }
}