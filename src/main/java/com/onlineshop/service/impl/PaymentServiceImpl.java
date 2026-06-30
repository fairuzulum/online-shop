package com.onlineshop.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.exception.ResourceNotFoundException;
import com.onlineshop.model.Order;
import com.onlineshop.model.Payment;
import com.onlineshop.repository.OrderRepository;
import com.onlineshop.repository.PaymentRepository;
import com.onlineshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.snap-url}")
    private String snapUrl;

    @Override
    public String createSnapTransaction(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", order.getMidtransOrderId());
        transactionDetails.put("gross_amount", order.getTotalAmount().intValue());

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("email", order.getUser().getEmail());
        customerDetails.put("first_name", order.getUser().getName());

        Map<String, Object> body = new HashMap<>();
        body.put("transaction_details", transactionDetails);
        body.put("customer_details", customerDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = Base64.getEncoder().encodeToString((serverKey + ":").getBytes());
        headers.set("Authorization", "Basic " + auth);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        JsonNode response = restTemplate.postForObject(snapUrl, request, JsonNode.class);

        // Simpan record Payment dengan status PENDING
        Payment payment = Payment.builder()
                .order(order)
                .status(Payment.PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        return response.get("redirect_url").asText();
    }

    @Override
    @Transactional
    public void handleNotification(String payload) {
        try {
            JsonNode notification = objectMapper.readTree(payload);

            String midtransOrderId = notification.get("order_id").asText();
            String transactionStatus = notification.get("transaction_status").asText();
            String signatureKey = notification.get("signature_key").asText();
            String statusCode = notification.get("status_code").asText();
            String grossAmount = notification.get("gross_amount").asText();

            // Verifikasi signature — WAJIB, biar yakin notif ini beneran dari Midtrans
            String expectedSignature = sha512(midtransOrderId + statusCode + grossAmount + serverKey);
            if (!expectedSignature.equals(signatureKey)) {
                throw new SecurityException("Invalid signature, possible fraud notification");
            }

            Order order = orderRepository.findByMidtransOrderId(midtransOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + midtransOrderId));

            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + midtransOrderId));

            switch (transactionStatus) {
                case "settlement", "capture" -> {
                    payment.setStatus(Payment.PaymentStatus.SETTLEMENT);
                    payment.setPaidAt(java.time.LocalDateTime.now());
                    order.setStatus(Order.OrderStatus.PAID);
                }
                case "expire" -> {
                    payment.setStatus(Payment.PaymentStatus.EXPIRED);
                    order.setStatus(Order.OrderStatus.CANCELLED);
                }
                case "deny", "cancel" -> {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    order.setStatus(Order.OrderStatus.FAILED);
                }
                default -> {
                    // pending atau status lain, gak perlu diapa-apain
                }
            }

            String transactionId = notification.has("transaction_id") ? notification.get("transaction_id").asText() : null;
            payment.setMidtransTransactionId(transactionId);

            String paymentType = notification.has("payment_type") ? notification.get("payment_type").asText() : null;
            payment.setPaymentMethod(paymentType);

            paymentRepository.save(payment);
            orderRepository.save(order);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process notification: " + e.getMessage(), e);
        }
    }

    private String sha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}