package com.chatkeluhan.demo.dto;

public class PasswordResetRequest {
    private String method; // values: "EMAIL", "2FA", "GOOGLE"
    private String identifier; // email address or employee ID
    private String token; // optional

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
