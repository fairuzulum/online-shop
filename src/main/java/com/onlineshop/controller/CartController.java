package com.onlineshop.controller;

import com.onlineshop.dto.request.AddToCartRequest;
import com.onlineshop.dto.response.ApiResponse;
import com.onlineshop.dto.response.CartResponse;
import com.onlineshop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cartService.getCart(authentication.getName())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartService.addItem(authentication.getName(), request)));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cartService.updateItemQuantity(authentication.getName(), cartItemId, quantity)));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            Authentication authentication,
            @PathVariable Long cartItemId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Item removed", cartService.removeItem(authentication.getName(), cartItemId)));
    }
}