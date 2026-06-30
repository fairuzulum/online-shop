package com.onlineshop.service;

import com.onlineshop.model.Category;

import java.util.List;

public interface CategoryService {
    Category create(String name);
    List<Category> getAll();
    void delete(Long id);
}