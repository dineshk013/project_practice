package com.revcart.orderservice.client;

import com.revcart.orderservice.dto.AddressDto;
import com.revcart.orderservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    ApiResponse<Object> getUserById(@PathVariable Long id);
    
    @GetMapping("/api/users/addresses")
    ApiResponse<java.util.List<AddressDto>> getAddresses(@RequestHeader("X-User-Id") Long userId);
    
    @GetMapping("/api/admin/users")
    ApiResponse<Object> getAllUsers();
    
    @GetMapping("/api/admin/count/active")
    Long getActiveUsersCount();
    
    @GetMapping("/api/admin/delivery-agents")
    ApiResponse<java.util.List<Object>> getDeliveryAgents();
}
