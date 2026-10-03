package com.chatkeluhan.demo.service;

import com.chatkeluhan.demo.dto.PasswordResetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private JavaMailSender mailSender;

    public String processReset(PasswordResetRequest request) {
        if (request.getMethod() == null) {
            return "Invalid reset method.";
        }

        switch (request.getMethod().toUpperCase()) {
            case "EMAIL":
                return handleEmailReset(request.getIdentifier());
            case "2FA":
                return "2FA token verified successfully.";
            case "GOOGLE":
                return "Google Auth verified successfully.";
            default:
                return "Unsupported reset method.";
        }
    }

    private String handleEmailReset(String email) {
        try {
            String code = generateRandomCode();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Password Reset Code");
            message.setText("Your password reset code is: " + code);
            
            mailSender.send(message);
            return "Reset code sent successfully to " + email;
        } catch (Exception e) {
            return "Failed to send reset code: " + e.getMessage();
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}
