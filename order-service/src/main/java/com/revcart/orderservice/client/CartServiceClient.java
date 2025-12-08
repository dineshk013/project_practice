package com.revcart.orderservice.client;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service", url = "${services.cart-service.url}")
public interface CartServiceClient {
    
    @GetMapping("/api/cart")
    ApiResponse<CartDto> getCart(@RequestHeader("X-User-Id") Long userId);
    
    @PostMapping("/api/cart/validate")
    ApiResponse<Boolean> validateCart(@RequestHeader("X-User-Id") Long userId);
    
    @DeleteMapping("/api/cart/clear")
    ApiResponse<Void> clearCart(@RequestHeader("X-User-Id") Long userId);
}
