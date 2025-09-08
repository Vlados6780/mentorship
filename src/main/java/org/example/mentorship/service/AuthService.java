package org.example.mentorship.service;

import org.example.mentorship.dto.JwtResponse;
import org.example.mentorship.dto.LoginRequest;
import org.example.mentorship.dto.RegisterRequest;

public interface AuthService {
    JwtResponse authenticate(LoginRequest authRequest);
    void register(RegisterRequest authRequest);
    Integer getUserIdByEmail(String email);

    String getUserRoleByEmail(String userEmail);
    void deleteUserByEmail(String email);
}
