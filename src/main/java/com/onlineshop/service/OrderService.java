package com.onlineshop.service;

import com.onlineshop.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse checkout(String userEmail);
    List<OrderResponse> getMyOrders(String userEmail);
    OrderResponse getOrderById(Long orderId, String userEmail);
}