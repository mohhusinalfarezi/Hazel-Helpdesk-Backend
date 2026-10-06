package com.chatkeluhan.demo.controller;

import com.chatkeluhan.demo.dto.LoginRequest;
import com.chatkeluhan.demo.dto.RegisterRequest;
import com.chatkeluhan.demo.dto.PasswordResetRequest;
import com.chatkeluhan.demo.entity.UserEntity;
import com.chatkeluhan.demo.repository.UserRepository;
import com.chatkeluhan.demo.service.AuthService;
import com.chatkeluhan.demo.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email already registered");
            return ResponseEntity.badRequest().body(response);
        }

        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        java.util.Optional<UserEntity> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("token", "dummy-jwt-token-12345");
            response.put("message", "Login successful");
            // Menambahkan nama pengguna ke dalam response
            response.put("name", userOpt.get().getName());
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequest request) {
        String message = passwordResetService.processReset(request);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}