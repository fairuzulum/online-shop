package com.onlineshop.service;

import com.onlineshop.dto.request.ProductRequest;
import com.onlineshop.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request, String adminEmail);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    ProductResponse getById(Long id);
    List<ProductResponse> getAll();
    List<ProductResponse> getByCategory(Long categoryId);
}