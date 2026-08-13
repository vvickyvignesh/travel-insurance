package com.travelinsurance.controller;

import com.travelinsurance.dto.AuthResponse;
import com.travelinsurance.dto.LoginRequest;
import com.travelinsurance.dto.RegisterRequest;
import com.travelinsurance.dto.UserDto;
import com.travelinsurance.entity.UserRole;
import com.travelinsurance.entity.User;
import com.travelinsurance.exception.CustomException;
import com.travelinsurance.repository.UserRepository;
import com.travelinsurance.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException("Email is already registered", HttpStatus.CONFLICT);
        }

        // Determine if this is the first user registered in the system.
        // If it is, default to ADMIN role so we can test ADMIN endpoints easily.
        // Otherwise, default to USER role.
        UserRole role = userRepository.count() == 0 ? UserRole.ADMIN : UserRole.USER;

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phone(registerRequest.getPhone())
                .role(role)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .message("Registration successful")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(AuthResponse.builder()
                .success(true)
                .token(token)
                .user(userDto)
                .build());
    }
}
