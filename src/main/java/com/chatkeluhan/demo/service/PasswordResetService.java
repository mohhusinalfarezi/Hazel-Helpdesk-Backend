package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.entity.UserEntity;
import com.chatkeluhan.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    public String generateAndSendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        user.setOtpCode(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        System.out.println("SIMULATION: Email sent to " + email + " with OTP: " + otp);

        return "OTP sent successfully";
    }

    public boolean verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        return user.getOtpCode() != null
                && user.getOtpCode().equals(otp)
                && user.getOtpExpiry() != null
                && java.time.LocalDateTime.now().isBefore(user.getOtpExpiry());
    }

    public void updatePassword(String email, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        user.setPassword(newPassword);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }
}
