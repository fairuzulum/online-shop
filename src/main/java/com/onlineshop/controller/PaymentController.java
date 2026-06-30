package com.onlineshop.controller;

import com.onlineshop.dto.response.ApiResponse;
import com.onlineshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<String>> pay(Authentication authentication, @PathVariable Long orderId) {
        String redirectUrl = paymentService.createSnapTransaction(orderId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Snap transaction created", redirectUrl));
    }

    @PostMapping("/notification")
    public ResponseEntity<String> notification(@RequestBody String payload) {
        paymentService.handleNotification(payload);
        return ResponseEntity.ok("OK");
    }
}