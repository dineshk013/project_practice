package com.revcart.userservice.controller;

import com.revcart.userservice.dto.*;
import com.revcart.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/users/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/api/users/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        authService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP verified successfully"));
    }

    @PostMapping("/api/users/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        authService.generateOtp(email);
        // After OTP verification, reset password
        authService.resetPassword(email, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    @PostMapping("/api/users/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestBody String token) {
        boolean valid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(valid, "Token validation completed"));
    }

    @GetMapping("/api/users/test-bcrypt")
    public ResponseEntity<String> testBcrypt() {
        return ResponseEntity.ok(authService.testAdminBcrypt());
    }

    @GetMapping("/api/users/generate-hash")
    public ResponseEntity<String> generateHash(@RequestParam String password) {
        return ResponseEntity.ok(authService.generateBcryptHash(password));
    }
    
    @PostMapping("/api/users/test-email")
    public ResponseEntity<ApiResponse<String>> testEmail(@RequestParam String email) {
        authService.sendTestEmail(email);
        return ResponseEntity.ok(ApiResponse.success(
            "Test email sent to " + email, 
            "Check your inbox. If you don't receive it, verify MAIL_USERNAME and MAIL_PASSWORD env vars."
        ));
    }
    
    @PostMapping("/api/users/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestParam String email) {
        authService.generateOtp(email);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully"));
    }
}
