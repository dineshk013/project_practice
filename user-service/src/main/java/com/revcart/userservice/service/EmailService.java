package com.revcart.userservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otp, String userName) {
        log.info("Attempting to send OTP email to: {} from: {}", toEmail, fromEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("RevCart - Email Verification OTP");
            helper.setText(buildOtpEmailContent(otp, userName), true);

            mailSender.send(message);
            log.info("✅ OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            log.error("Check SMTP configuration: host=smtp.gmail.com, port=587, username={}", fromEmail);
            log.error("Ensure MAIL_USERNAME and MAIL_PASSWORD environment variables are set correctly");
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }
    
    public void sendTestEmail(String toEmail) {
        log.info("Sending test email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("RevCart - Test Email");
            helper.setText("<h1>Test Email from RevCart</h1><p>If you received this, your email configuration is working correctly!</p>", true);

            mailSender.send(message);
            log.info("✅ Test email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send test email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send test email: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String otp, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("RevCart - Password Reset OTP");
            helper.setText(buildPasswordResetEmailContent(otp, userName), true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildOtpEmailContent(String otp, String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .otp-box { background: white; border: 2px dashed #10b981; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #10b981; letter-spacing: 8px; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    .button { background: #10b981; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; display: inline-block; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛒 RevCart</h1>
                        <p>Email Verification</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Thank you for registering with RevCart. To complete your registration, please verify your email address using the OTP below:</p>
                        
                        <div class="otp-box">
                            <p style="margin: 0; color: #6b7280;">Your OTP Code</p>
                            <div class="otp-code">%s</div>
                            <p style="margin: 10px 0 0 0; color: #6b7280; font-size: 14px;">Valid for 5 minutes</p>
                        </div>
                        
                        <p><strong>Important:</strong></p>
                        <ul>
                            <li>This OTP is valid for 5 minutes only</li>
                            <li>Do not share this OTP with anyone</li>
                            <li>If you didn't request this, please ignore this email</li>
                        </ul>
                        
                        <p>Welcome to RevCart - Your one-stop shop for everything!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 RevCart. All rights reserved.</p>
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, otp);
    }

    private String buildPasswordResetEmailContent(String otp, String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ef4444 0%%, #dc2626 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .otp-box { background: white; border: 2px dashed #ef4444; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #ef4444; letter-spacing: 8px; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    .warning { background: #fef2f2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛒 RevCart</h1>
                        <p>Password Reset Request</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>We received a request to reset your password. Use the OTP below to proceed:</p>
                        
                        <div class="otp-box">
                            <p style="margin: 0; color: #6b7280;">Your OTP Code</p>
                            <div class="otp-code">%s</div>
                            <p style="margin: 10px 0 0 0; color: #6b7280; font-size: 14px;">Valid for 5 minutes</p>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Security Alert:</strong>
                            <p style="margin: 5px 0 0 0;">If you didn't request a password reset, please ignore this email and ensure your account is secure.</p>
                        </div>
                        
                        <p><strong>Important:</strong></p>
                        <ul>
                            <li>This OTP expires in 5 minutes</li>
                            <li>Never share your OTP with anyone</li>
                            <li>RevCart will never ask for your OTP via phone or email</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>© 2024 RevCart. All rights reserved.</p>
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, otp);
    }
}
