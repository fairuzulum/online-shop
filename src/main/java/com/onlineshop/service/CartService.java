package com.onlineshop.service;

import com.onlineshop.dto.request.AddToCartRequest;
import com.onlineshop.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String userEmail);
    CartResponse addItem(String userEmail, AddToCartRequest request);
    CartResponse updateItemQuantity(String userEmail, Long cartItemId, Integer quantity);
    CartResponse removeItem(String userEmail, Long cartItemId);
}