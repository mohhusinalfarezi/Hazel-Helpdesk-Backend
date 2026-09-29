package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.dto.auth.AuthResponse;
import com.chatkeluhan.demo.dto.auth.LoginRequest;
import com.chatkeluhan.demo.dto.auth.RegisterRequest;
import com.chatkeluhan.demo.entity.Customer;
import com.chatkeluhan.demo.repository.CustomerRepository;
import com.chatkeluhan.demo.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest request) {
        // Cek apakah email sudah digunakan
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }

        Customer customer = new Customer();
        customer.setCustomerId(request.getCustomerId());
        customer.setFullName(request.getFullName());
        customer.setContactNumber(request.getContactNumber());
        customer.setEmail(request.getEmail());

        // Enkripsi password menggunakan BCryptPasswordEncoder
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        customerRepository.save(customer);

        return new AuthResponse(null, "Registrasi berhasil");
    }

    public AuthResponse login(LoginRequest request) {
        // Cari pengguna berdasarkan email
        Optional<Customer> customerOpt = customerRepository.findByEmail(request.getEmail());

        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Email atau password salah");
        }

        Customer customer = customerOpt.get();

        // Validasi password
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Email atau password salah");
        }

        // Generate JWT token (gunakan email sebagai subject/username di token)
        String token = jwtUtils.generateToken(customer.getEmail());

        return new AuthResponse(token, "Login berhasil");
    }
}
