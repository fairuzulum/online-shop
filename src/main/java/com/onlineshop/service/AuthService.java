package com.onlineshop.service;

import com.onlineshop.dto.request.LoginRequest;
import com.onlineshop.dto.request.RegisterRequest;
import com.onlineshop.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}