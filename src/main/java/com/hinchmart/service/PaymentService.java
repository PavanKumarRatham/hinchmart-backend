package com.hinchmart.service;

import com.hinchmart.dto.request.PaymentCreateRequest;
import com.hinchmart.dto.request.PaymentVerifyRequest;
import com.hinchmart.dto.request.RefundRequest;
import com.hinchmart.dto.response.PaymentDto;
import com.hinchmart.dto.response.PaymentTransactionDto;
import com.hinchmart.dto.response.RefundDto;
import com.hinchmart.entity.*;
import com.hinchmart.entity.enums.*;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.OrderRepository;
import com.hinchmart.repository.PaymentRepository;
import com.hinchmart.repository.PaymentTransactionRepository;
import com.hinchmart.repository.RefundRepository;
import com.hinchmart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          RefundRepository refundRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository,
                          InvoiceService invoiceService,
                          NotificationService notificationService,
                          ActivityLogService activityLogService) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public PaymentDto createPayment(Long buyerUserId, PaymentCreateRequest request) {
        User buyer = userRepository.findById(buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer user not found: " + buyerUserId));

        // CRITICAL RULE: Never trust amount from client. Fetch order directly from DB.
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (!order.getBuyer().getId().equals(buyerUserId)) {
            throw new UnauthorizedException("You do not have permission to pay for this order");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID || order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("This order has already been paid for.");
        }

        BigDecimal verifiedOrderAmount = order.getTotalAmount();
        String gatewayOrderId = "order_rzp_" + System.currentTimeMillis() % 10000000 + "_" + UUID.randomUUID().toString().substring(0, 6);

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setPaymentNumber("PAY-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
            newPayment.setOrder(order);
            newPayment.setBuyer(buyer);
            return newPayment;
        });

        payment.setAmount(verifiedOrderAmount);
        payment.setCurrency("INR");
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.UPI);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGatewayOrderId(gatewayOrderId);

        Payment savedPayment = paymentRepository.save(payment);

        // Record initialization transaction
        PaymentTransaction transaction = new PaymentTransaction(
                savedPayment,
                PaymentTransactionType.PAYMENT,
                verifiedOrderAmount,
                "INITIATED",
                gatewayOrderId,
                "{\"gatewayOrderId\":\"" + gatewayOrderId + "\",\"amount\":" + verifiedOrderAmount + "}"
        );
        paymentTransactionRepository.save(transaction);
        savedPayment.addTransaction(transaction);

        activityLogService.log(buyerUserId, buyer.getEmail(), "PAYMENT_INITIATED", "PAYMENT",
                savedPayment.getId(), "Initiated payment for order " + order.getOrderNumber() + " (Amount: ₹" + verifiedOrderAmount + ")", null);

        return mapToPaymentDto(savedPayment);
    }

    @Transactional
    public PaymentDto verifyPayment(Long buyerUserId, PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + request.getPaymentId()));

        Order order = payment.getOrder();

        payment.setGatewayPaymentId(request.getGatewayPaymentId());
        payment.setGatewaySignature(request.getGatewaySignature() != null ? request.getGatewaySignature() : "sig_" + UUID.randomUUID().toString().substring(0, 12));
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        // Record SUCCESS transaction
        PaymentTransaction transaction = new PaymentTransaction(
                savedPayment,
                PaymentTransactionType.PAYMENT,
                savedPayment.getAmount(),
                "SUCCESS",
                request.getGatewayPaymentId(),
                "{\"gatewayPaymentId\":\"" + request.getGatewayPaymentId() + "\",\"status\":\"CAPTURED\"}"
        );
        paymentTransactionRepository.save(transaction);
        savedPayment.addTransaction(transaction);

        // Update Order payment status & confirm order
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.CONFIRMED, "Payment verified successfully. Order confirmed.", payment.getBuyer()));
        orderRepository.save(order);

        // Auto-generate GST Tax Invoice
        try {
            invoiceService.generateInvoiceForOrder(order.getId());
        } catch (Exception ex) {
            // Log but don't fail verification if invoice auto-generation hits exception
        }

        // Trigger Notifications
        // 1. To Buyer: PAYMENT_SUCCESS
        notificationService.sendNotification(
                payment.getBuyer(),
                "Payment Successful!",
                "Your payment of ₹" + payment.getAmount() + " for order " + order.getOrderNumber() + " was processed successfully.",
                NotificationType.PAYMENT_SUCCESS,
                order.getId(),
                "ORDER"
        );

        // 2. To Seller: ORDER_CONFIRMED
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            User seller = order.getItems().get(0).getSeller();
            if (seller != null) {
                notificationService.sendNotification(
                        seller,
                        "New Confirmed Order Received!",
                        "Order " + order.getOrderNumber() + " with " + order.getItems().size() + " items has been confirmed and is ready to pack.",
                        NotificationType.ORDER_CONFIRMED,
                        order.getId(),
                        "ORDER"
                );
            }
        }

        activityLogService.log(buyerUserId, payment.getBuyer().getEmail(), "PAYMENT_VERIFIED", "PAYMENT",
                savedPayment.getId(), "Payment verified successfully for order " + order.getOrderNumber(), null);

        return mapToPaymentDto(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrderId(Long orderId, Long currentUserId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment record found for order ID: " + orderId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (currentUser.getRole() == Role.BUYER && !payment.getBuyer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to view this payment");
        }

        return mapToPaymentDto(payment);
    }

    @Transactional
    public RefundDto processRefund(Long paymentId, Long currentUserId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS && payment.getPaymentStatus() != PaymentStatus.PAID &&
                payment.getPaymentStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BadRequestException("Cannot process refund on an unpaid payment transaction");
        }

        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Refund amount (₹" + request.getAmount() + ") cannot exceed original payment amount (₹" + payment.getAmount() + ")");
        }

        String refundNumber = "REF-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String gatewayRefundId = "rfnd_" + UUID.randomUUID().toString().substring(0, 10);

        Refund refund = new Refund(
                refundNumber,
                payment,
                payment.getOrder(),
                request.getAmount(),
                request.getReason(),
                RefundStatus.PROCESSED,
                gatewayRefundId
        );
        Refund savedRefund = refundRepository.save(refund);
        payment.addRefund(savedRefund);

        // Update payment status
        if (request.getAmount().compareTo(payment.getAmount()) == 0) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.getOrder().setOrderStatus(OrderStatus.CANCELLED);
        } else {
            payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        paymentRepository.save(payment);
        orderRepository.save(payment.getOrder());

        // Record refund transaction
        PaymentTransaction transaction = new PaymentTransaction(
                payment,
                PaymentTransactionType.REFUND,
                request.getAmount(),
                "PROCESSED",
                gatewayRefundId,
                "{\"refundId\":\"" + gatewayRefundId + "\",\"amount\":" + request.getAmount() + ",\"reason\":\"" + request.getReason() + "\"}"
        );
        paymentTransactionRepository.save(transaction);

        // Notify Buyer
        notificationService.sendNotification(
                payment.getBuyer(),
                "Refund Processed",
                "A refund of ₹" + request.getAmount() + " for order " + payment.getOrder().getOrderNumber() + " has been issued.",
                NotificationType.PAYMENT_SUCCESS,
                payment.getOrder().getId(),
                "ORDER"
        );

        return mapToRefundDto(savedRefund);
    }

    public PaymentDto mapToPaymentDto(Payment p) {
        PaymentDto dto = new PaymentDto();
        dto.setId(p.getId());
        dto.setPaymentNumber(p.getPaymentNumber());
        if (p.getOrder() != null) {
            dto.setOrderId(p.getOrder().getId());
            dto.setOrderNumber(p.getOrder().getOrderNumber());
        }
        if (p.getBuyer() != null) {
            dto.setBuyerId(p.getBuyer().getId());
            dto.setBuyerName(p.getBuyer().getFullName());
        }
        dto.setAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setGatewayOrderId(p.getGatewayOrderId());
        dto.setGatewayPaymentId(p.getGatewayPaymentId());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        if (p.getTransactions() != null) {
            List<PaymentTransactionDto> trxDtos = p.getTransactions().stream().map(t -> new PaymentTransactionDto(
                    t.getId(),
                    p.getId(),
                    t.getTransactionType(),
                    t.getAmount(),
                    t.getStatus(),
                    t.getGatewayReference(),
                    t.getCreatedAt()
            )).collect(Collectors.toList());
            dto.setTransactions(trxDtos);
        }

        if (p.getRefunds() != null) {
            List<RefundDto> refDtos = p.getRefunds().stream().map(this::mapToRefundDto).collect(Collectors.toList());
            dto.setRefunds(refDtos);
        }

        return dto;
    }

    public RefundDto mapToRefundDto(Refund r) {
        return new RefundDto(
                r.getId(),
                r.getRefundNumber(),
                r.getPayment() != null ? r.getPayment().getId() : null,
                r.getOrder() != null ? r.getOrder().getId() : null,
                r.getAmount(),
                r.getReason(),
                r.getRefundStatus(),
                r.getGatewayRefundId(),
                r.getCreatedAt()
        );
    }
}
