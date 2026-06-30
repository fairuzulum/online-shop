package com.onlineshop.controller;

import com.onlineshop.dto.response.ApiResponse;
import com.onlineshop.dto.response.OrderResponse;
import com.onlineshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Checkout successful", orderService.checkout(authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orderService.getMyOrders(authentication.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", orderService.getOrderById(id, authentication.getName())));
    }
}