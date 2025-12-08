package com.revcart.deliveryservice.client;

import com.revcart.deliveryservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    ApiResponse<Object> getUserById(@PathVariable Long id);
}
