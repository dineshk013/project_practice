package com.revcart.userservice.service;

import com.revcart.userservice.dto.*;
import com.revcart.userservice.entity.OtpToken;
import com.revcart.userservice.entity.User;
import com.revcart.userservice.exception.BadRequestException;
import com.revcart.userservice.repository.OtpTokenRepository;
import com.revcart.userservice.repository.UserRepository;
import com.revcart.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setName(request.getName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        
        // Handle role assignment
        User.Role selectedRole = User.Role.CUSTOMER; // default
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String roleStr = request.getRole().trim().toUpperCase();
            if (!java.util.List.of("CUSTOMER", "DELIVERY_AGENT", "ADMIN").contains(roleStr)) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
            selectedRole = User.Role.valueOf(roleStr);
        }
        user.setRole(selectedRole);
        user.setActive(true);

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());

        String token = jwtTokenProvider.generateToken(saved.getEmail(), saved.getId());
        return new AuthResponse(token, toDto(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword().trim();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getActive()) {
            throw new BadRequestException("Account is inactive");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
        log.info("User logged in: {}", user.getEmail());
        return new AuthResponse(token, toDto(user));
    }

    @Transactional
    public void generateOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email not found"));

        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtp(otp);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);

        otpTokenRepository.save(otpToken);
        
        // Send OTP email
        emailService.sendOtpEmail(email, otp, user.getName());
        log.info("OTP generated and sent to: {}", email);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        OtpToken otpToken = otpTokenRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        log.info("OTP verified for: {}", email);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for: {}", email);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public String testAdminBcrypt() {
        try {
            User admin = userRepository.findByEmailIgnoreCase("admin@revcart.com")
                    .orElse(null);
            
            if (admin == null) {
                return "ADMIN USER NOT FOUND IN DATABASE";
            }
            
            String rawPassword = "admin123";
            String storedHash = admin.getPassword();
            boolean matches = passwordEncoder.matches(rawPassword, storedHash);
            
            StringBuilder result = new StringBuilder();
            result.append("BCrypt Test Results:\n");
            result.append("Email: ").append(admin.getEmail()).append("\n");
            result.append("Role: ").append(admin.getRole()).append("\n");
            result.append("Active: ").append(admin.getActive()).append("\n");
            result.append("Raw Password: ").append(rawPassword).append("\n");
            result.append("Stored Hash: ").append(storedHash).append("\n");
            result.append("Hash Length: ").append(storedHash.length()).append("\n");
            result.append("BCrypt Matches: ").append(matches ? "TRUE" : "FALSE").append("\n");
            
            if (!matches) {
                result.append("\nDIAGNOSIS: The stored hash does NOT match 'admin123'\n");
                result.append("SOLUTION: Run this SQL to fix:\n");
                result.append("UPDATE users SET password='$2a$10$dXJ3SW6G7P37LKLsOMufOeWIaqukjcChmMqrgM4.Qr3OPUgKqUaGC' WHERE email='admin@revcart.com';");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String generateBcryptHash(String password) {
        String hash = passwordEncoder.encode(password);
        StringBuilder result = new StringBuilder();
        result.append("Password: ").append(password).append("\n");
        result.append("BCrypt Hash: ").append(hash).append("\n\n");
        result.append("SQL to update admin:\n");
        result.append("UPDATE users SET password='").append(hash).append("' WHERE email='admin@revcart.com';");
        return result.toString();
    }
    
    public void sendTestEmail(String email) {
        log.info("Sending test email to: {}", email);
        emailService.sendTestEmail(email);
    }
}
