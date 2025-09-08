package org.example.mentorship.service;

import org.example.mentorship.dto.JwtResponse;
import org.example.mentorship.dto.LoginRequest;
import org.example.mentorship.dto.RegisterRequest;
import org.example.mentorship.entity.Role;
import org.example.mentorship.entity.User;
import org.example.mentorship.repository.*;
import org.example.mentorship.security.CustomUserDetails;
import org.example.mentorship.security.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultAuthService implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultAuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtTokenUtils jwtTokenUtils,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenUtils = jwtTokenUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtResponse authenticate(LoginRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.email(),
                        authRequest.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenUtils.generateToken(userDetails);

        // Update user's online status
        User user = userDetails.getUser();
        user.setOnline(true);
        userRepository.save(user);

        return new JwtResponse(token);
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        // Check if the user already exists
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new RuntimeException("User with email " + registerRequest.email() + " already exists");
        }

        // Check and retrieve the requested role
        String roleName = registerRequest.role();
        if (roleName == null || (!roleName.equals("ROLE_STUDENT") && !roleName.equals("ROLE_MENTOR"))) {
            throw new RuntimeException("Invalid role. Valid values: ROLE_STUDENT, ROLE_MENTOR");
        }

        // Retrieve the role from the database
        Role userRole = roleRepository.findByRoleName(roleName);
        if (userRole == null) {
            throw new RuntimeException("Role " + roleName + " not found in the system");
        }

        // Create a new user
        User user = new User();
        user.setEmail(registerRequest.email());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        user.setRole(userRole);
        user.setEmailVerified(false);

        userRepository.save(user);
    }

    @Override
    public Integer getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
        return user.getId();
    }

    @Override
    public String getUserRoleByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
        return user.getRole().getRoleName();
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
        userRepository.delete(user);
    }
}
