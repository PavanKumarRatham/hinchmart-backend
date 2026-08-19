package com.hinchmart.service;

import com.hinchmart.dto.response.InvoiceDto;
import com.hinchmart.dto.response.InvoiceItemDto;
import com.hinchmart.entity.*;
import com.hinchmart.entity.enums.PaymentStatus;
import com.hinchmart.entity.enums.Role;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.InvoiceItemRepository;
import com.hinchmart.repository.InvoiceRepository;
import com.hinchmart.repository.OrderRepository;
import com.hinchmart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository invoiceItemRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository,
                          ActivityLogService activityLogService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public InvoiceDto generateInvoiceForOrder(Long orderId) {
        Optional<Invoice> existingOpt = invoiceRepository.findByOrderId(orderId);
        if (existingOpt.isPresent()) {
            return mapToInvoiceDto(existingOpt.get());
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        User buyer = order.getBuyer();
        BuyerProfile bp = buyer.getBuyerProfile();

        // Identify primary seller from order items
        User seller = null;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            seller = order.getItems().get(0).getSeller();
        }
        SellerProfile sp = (seller != null) ? seller.getSellerProfile() : null;

        String sellerState = (sp != null && sp.getState() != null) ? sp.getState().trim() : "Telangana";
        String buyerState = (order.getState() != null && !order.getState().trim().isEmpty()) ?
                order.getState().trim() :
                ((bp != null && bp.getState() != null) ? bp.getState().trim() : "Telangana");

        boolean isIntraState = sellerState.equalsIgnoreCase(buyerState);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-2026-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        invoice.setOrder(order);
        invoice.setOrderNumber(order.getOrderNumber());
        invoice.setSeller(seller);
        invoice.setSellerName(seller != null ? seller.getFullName() : "HinchMart Seller");
        invoice.setSellerCompanyName(sp != null ? sp.getCompanyName() : "HinchMart Certified Seller");
        invoice.setSellerGstin(sp != null ? sp.getGstin() : "36AAACH1234F1Z5");
        invoice.setBuyer(buyer);
        invoice.setBuyerName(buyer.getFullName());
        invoice.setBuyerCompanyName(bp != null ? bp.getCompanyName() : buyer.getFullName() + " Enterprise");
        invoice.setBuyerGstin(bp != null ? bp.getGstin() : "Unregistered");
        invoice.setBillingAddress(order.getBillingAddress());
        invoice.setShippingAddress(order.getShippingAddress());
        invoice.setPlaceOfSupply(buyerState);
        invoice.setIntraState(isIntraState);
        invoice.setDeliveryCharge(order.getDeliveryCharge() != null ? order.getDeliveryCharge() : BigDecimal.ZERO);
        invoice.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : PaymentStatus.PAID);
        invoice.setInvoiceDate(LocalDate.now());

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        if (order.getItems() != null) {
            for (OrderItem orderItem : order.getItems()) {
                Product product = orderItem.getProduct();
                BigDecimal itemTaxable = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal gstRate = orderItem.getGstPercentage() != null ? orderItem.getGstPercentage() : new BigDecimal("18.00");

                InvoiceItem invItem = new InvoiceItem();
                invItem.setInvoice(invoice);
                invItem.setProduct(product);
                invItem.setProductName(orderItem.getProductName());
                invItem.setHsnCode(product != null && product.getHsnCode() != null ? product.getHsnCode() : "7214");
                invItem.setQuantity(orderItem.getQuantity());
                invItem.setUnit(orderItem.getUnit());
                invItem.setUnitPrice(orderItem.getUnitPrice());
                invItem.setTaxableValue(itemTaxable);
                invItem.setGstRate(gstRate);

                if (isIntraState) {
                    // Intra-State (e.g. Telangana -> Telangana): Split into CGST (9%) + SGST (9%)
                    BigDecimal halfRate = gstRate.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                    BigDecimal cgst = itemTaxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal sgst = itemTaxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    invItem.setCgstRate(halfRate);
                    invItem.setCgstAmount(cgst);
                    invItem.setSgstRate(halfRate);
                    invItem.setSgstAmount(sgst);
                    invItem.setIgstRate(BigDecimal.ZERO);
                    invItem.setIgstAmount(BigDecimal.ZERO);
                    invItem.setTotalAmount(itemTaxable.add(cgst).add(sgst));

                    totalCgst = totalCgst.add(cgst);
                    totalSgst = totalSgst.add(sgst);
                } else {
                    // Inter-State: Apply IGST (18%)
                    BigDecimal igst = itemTaxable.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    invItem.setCgstRate(BigDecimal.ZERO);
                    invItem.setCgstAmount(BigDecimal.ZERO);
                    invItem.setSgstRate(BigDecimal.ZERO);
                    invItem.setSgstAmount(BigDecimal.ZERO);
                    invItem.setIgstRate(gstRate);
                    invItem.setIgstAmount(igst);
                    invItem.setTotalAmount(itemTaxable.add(igst));

                    totalIgst = totalIgst.add(igst);
                }

                invoiceItems.add(invItem);
                invoice.addItem(invItem);
                totalTaxable = totalTaxable.add(itemTaxable);
            }
        }

        BigDecimal totalGst = totalCgst.add(totalSgst).add(totalIgst);
        invoice.setTaxableValue(totalTaxable);
        invoice.setCgstAmount(totalCgst);
        invoice.setSgstAmount(totalSgst);
        invoice.setIgstAmount(totalIgst);
        invoice.setTotalGst(totalGst);
        invoice.setGrandTotal(totalTaxable.add(totalGst).add(invoice.getDeliveryCharge()));

        Invoice saved = invoiceRepository.save(invoice);

        activityLogService.log(buyer.getId(), buyer.getEmail(), "INVOICE_GENERATED", "INVOICE",
                saved.getId(), "Generated GST Invoice " + saved.getInvoiceNumber() + " for order " + order.getOrderNumber(), null);

        return mapToInvoiceDto(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceByOrderId(Long orderId, Long currentUserId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElseGet(() -> {
            // Auto-generate if not yet present
            return invoiceRepository.save(generateInvoiceEntity(orderId));
        });

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (currentUser.getRole() == Role.BUYER && !invoice.getBuyer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to view this invoice");
        }

        return mapToInvoiceDto(invoice);
    }

    private Invoice generateInvoiceEntity(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        User buyer = order.getBuyer();
        BuyerProfile bp = buyer.getBuyerProfile();

        User seller = null;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            seller = order.getItems().get(0).getSeller();
        }
        SellerProfile sp = (seller != null) ? seller.getSellerProfile() : null;

        String sellerState = (sp != null && sp.getState() != null) ? sp.getState().trim() : "Telangana";
        String buyerState = (order.getState() != null && !order.getState().trim().isEmpty()) ?
                order.getState().trim() :
                ((bp != null && bp.getState() != null) ? bp.getState().trim() : "Telangana");

        boolean isIntraState = sellerState.equalsIgnoreCase(buyerState);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-2026-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        invoice.setOrder(order);
        invoice.setOrderNumber(order.getOrderNumber());
        invoice.setSeller(seller);
        invoice.setSellerName(seller != null ? seller.getFullName() : "HinchMart Seller");
        invoice.setSellerCompanyName(sp != null ? sp.getCompanyName() : "HinchMart Certified Seller");
        invoice.setSellerGstin(sp != null ? sp.getGstin() : "36AAACH1234F1Z5");
        invoice.setBuyer(buyer);
        invoice.setBuyerName(buyer.getFullName());
        invoice.setBuyerCompanyName(bp != null ? bp.getCompanyName() : buyer.getFullName() + " Enterprise");
        invoice.setBuyerGstin(bp != null ? bp.getGstin() : "Unregistered");
        invoice.setBillingAddress(order.getBillingAddress());
        invoice.setShippingAddress(order.getShippingAddress());
        invoice.setPlaceOfSupply(buyerState);
        invoice.setIntraState(isIntraState);
        invoice.setDeliveryCharge(order.getDeliveryCharge() != null ? order.getDeliveryCharge() : BigDecimal.ZERO);
        invoice.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : PaymentStatus.PAID);
        invoice.setInvoiceDate(LocalDate.now());

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        if (order.getItems() != null) {
            for (OrderItem orderItem : order.getItems()) {
                Product product = orderItem.getProduct();
                BigDecimal itemTaxable = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                BigDecimal gstRate = orderItem.getGstPercentage() != null ? orderItem.getGstPercentage() : new BigDecimal("18.00");

                InvoiceItem invItem = new InvoiceItem();
                invItem.setInvoice(invoice);
                invItem.setProduct(product);
                invItem.setProductName(orderItem.getProductName());
                invItem.setHsnCode(product != null && product.getHsnCode() != null ? product.getHsnCode() : "7214");
                invItem.setQuantity(orderItem.getQuantity());
                invItem.setUnit(orderItem.getUnit());
                invItem.setUnitPrice(orderItem.getUnitPrice());
                invItem.setTaxableValue(itemTaxable);
                invItem.setGstRate(gstRate);

                if (isIntraState) {
                    BigDecimal halfRate = gstRate.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                    BigDecimal cgst = itemTaxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal sgst = itemTaxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    invItem.setCgstRate(halfRate);
                    invItem.setCgstAmount(cgst);
                    invItem.setSgstRate(halfRate);
                    invItem.setSgstAmount(sgst);
                    invItem.setIgstRate(BigDecimal.ZERO);
                    invItem.setIgstAmount(BigDecimal.ZERO);
                    invItem.setTotalAmount(itemTaxable.add(cgst).add(sgst));

                    totalCgst = totalCgst.add(cgst);
                    totalSgst = totalSgst.add(sgst);
                } else {
                    BigDecimal igst = itemTaxable.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    invItem.setCgstRate(BigDecimal.ZERO);
                    invItem.setCgstAmount(BigDecimal.ZERO);
                    invItem.setSgstRate(BigDecimal.ZERO);
                    invItem.setSgstAmount(BigDecimal.ZERO);
                    invItem.setIgstRate(gstRate);
                    invItem.setIgstAmount(igst);
                    invItem.setTotalAmount(itemTaxable.add(igst));

                    totalIgst = totalIgst.add(igst);
                }

                invoice.addItem(invItem);
                totalTaxable = totalTaxable.add(itemTaxable);
            }
        }

        BigDecimal totalGst = totalCgst.add(totalSgst).add(totalIgst);
        invoice.setTaxableValue(totalTaxable);
        invoice.setCgstAmount(totalCgst);
        invoice.setSgstAmount(totalSgst);
        invoice.setIgstAmount(totalIgst);
        invoice.setTotalGst(totalGst);
        invoice.setGrandTotal(totalTaxable.add(totalGst).add(invoice.getDeliveryCharge()));

        return invoice;
    }

    public InvoiceDto mapToInvoiceDto(Invoice inv) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(inv.getId());
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        if (inv.getOrder() != null) {
            dto.setOrderId(inv.getOrder().getId());
        }
        dto.setOrderNumber(inv.getOrderNumber());
        if (inv.getSeller() != null) {
            dto.setSellerId(inv.getSeller().getId());
        }
        dto.setSellerName(inv.getSellerName());
        dto.setSellerCompanyName(inv.getSellerCompanyName());
        dto.setSellerGstin(inv.getSellerGstin());
        if (inv.getBuyer() != null) {
            dto.setBuyerId(inv.getBuyer().getId());
        }
        dto.setBuyerName(inv.getBuyerName());
        dto.setBuyerCompanyName(inv.getBuyerCompanyName());
        dto.setBuyerGstin(inv.getBuyerGstin());
        dto.setBillingAddress(inv.getBillingAddress());
        dto.setShippingAddress(inv.getShippingAddress());
        dto.setPlaceOfSupply(inv.getPlaceOfSupply());
        dto.setIntraState(inv.isIntraState());
        dto.setTaxableValue(inv.getTaxableValue());
        dto.setCgstAmount(inv.getCgstAmount());
        dto.setSgstAmount(inv.getSgstAmount());
        dto.setIgstAmount(inv.getIgstAmount());
        dto.setTotalGst(inv.getTotalGst());
        dto.setDeliveryCharge(inv.getDeliveryCharge());
        dto.setGrandTotal(inv.getGrandTotal());
        dto.setPaymentStatus(inv.getPaymentStatus());
        dto.setInvoiceDate(inv.getInvoiceDate());
        dto.setCreatedAt(inv.getCreatedAt());

        if (inv.getItems() != null) {
            List<InvoiceItemDto> itemDtos = inv.getItems().stream().map(item -> {
                InvoiceItemDto iDto = new InvoiceItemDto();
                iDto.setId(item.getId());
                if (item.getProduct() != null) {
                    iDto.setProductId(item.getProduct().getId());
                }
                iDto.setProductName(item.getProductName());
                iDto.setHsnCode(item.getHsnCode());
                iDto.setQuantity(item.getQuantity());
                iDto.setUnit(item.getUnit());
                iDto.setUnitPrice(item.getUnitPrice());
                iDto.setTaxableValue(item.getTaxableValue());
                iDto.setGstRate(item.getGstRate());
                iDto.setCgstRate(item.getCgstRate());
                iDto.setCgstAmount(item.getCgstAmount());
                iDto.setSgstRate(item.getSgstRate());
                iDto.setSgstAmount(item.getSgstAmount());
                iDto.setIgstRate(item.getIgstRate());
                iDto.setIgstAmount(item.getIgstAmount());
                iDto.setTotalAmount(item.getTotalAmount());
                return iDto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
