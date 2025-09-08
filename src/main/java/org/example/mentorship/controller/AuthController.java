package org.example.mentorship.controller;

import org.example.mentorship.dto.JwtResponse;
import org.example.mentorship.dto.LoginRequest;
import org.example.mentorship.dto.RegisterRequest;
import org.example.mentorship.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AsyncTaskExecutor executor;

    @Autowired
    public AuthController(AuthService authService,
                          @Qualifier("securityAwareAsyncExecutor") AsyncTaskExecutor executor) {
        this.authService = authService;
        this.executor = executor;
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<?>> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JwtResponse jwtResponse = authService.authenticate(loginRequest);
                return ResponseEntity.ok(jwtResponse.token());
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }
        });
    }

    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<?>> registerUser(@RequestBody RegisterRequest registerRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                authService.register(registerRequest);
                Integer userId = authService.getUserIdByEmail(registerRequest.email());

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("userId", userId, "message", "User registered successfully"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        });
    }


    @DeleteMapping("/delete")
    public CompletableFuture<ResponseEntity<?>> deleteCurrentUser() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String currentUserEmail = authentication.getName();

                authService.deleteUserByEmail(currentUserEmail);
                return ResponseEntity.ok(Map.of("message", "Your account has been successfully deleted."));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Error while deleting: " + e.getMessage()));
            }
        }, executor);
    }

}
