package com.onlineshop.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;
}