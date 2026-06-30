package com.onlineshop.service.impl;

import com.onlineshop.dto.request.AddToCartRequest;
import com.onlineshop.dto.response.CartResponse;
import com.onlineshop.exception.ResourceNotFoundException;
import com.onlineshop.model.Cart;
import com.onlineshop.model.CartItem;
import com.onlineshop.model.Product;
import com.onlineshop.model.User;
import com.onlineshop.repository.CartItemRepository;
import com.onlineshop.repository.CartRepository;
import com.onlineshop.repository.ProductRepository;
import com.onlineshop.repository.UserRepository;
import com.onlineshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String userEmail, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userEmail, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item does not belong to this user");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        Cart cart = getOrCreateCart(userEmail);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item does not belong to this user");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    private Cart getOrCreateCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartResponse.CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalPrice(total)
                .build();
    }
}