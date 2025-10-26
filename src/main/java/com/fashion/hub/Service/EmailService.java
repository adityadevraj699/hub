package com.fashion.hub.Service;

public interface EmailService {
    void sendOtpEmail(String to, String subject, String otp) throws Exception;
}
