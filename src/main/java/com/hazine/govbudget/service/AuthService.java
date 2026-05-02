package com.hazine.govbudget.service;

import com.hazine.govbudget.dto.request.LoginRequest;
import com.hazine.govbudget.dto.request.RegisterRequest;
import com.hazine.govbudget.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    void logout(String token);
}