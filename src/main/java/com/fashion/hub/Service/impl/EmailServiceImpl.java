package com.fashion.hub.Service.impl;

import com.fashion.hub.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String subject, String otp) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);

            // HTML template for OTP
            String htmlContent = "<div style='font-family:sans-serif; text-align:center; padding:20px;'>"
                    + "<h2 style='color:#0d6efd;'>FashionHub Registration</h2>"
                    + "<p>Thank you for registering. Use the following OTP to verify your email:</p>"
                    + "<h3 style='background:#0d6efd; color:white; display:inline-block; padding:10px 20px; border-radius:8px;'>" 
                    + otp + "</h3>"
                    + "<p>This OTP is valid for 5 minutes only.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new Exception("Failed to send email: " + e.getMessage());
        }
    }
}
