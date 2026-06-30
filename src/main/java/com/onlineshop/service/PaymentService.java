package com.onlineshop.service;

public interface PaymentService {
    String createSnapTransaction(Long orderId, String userEmail);
    void handleNotification(String payload);
}