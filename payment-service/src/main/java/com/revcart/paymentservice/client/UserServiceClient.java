package com.revcart.paymentservice.client;

import com.revcart.paymentservice.dto.ApiResponse;
import com.revcart.paymentservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable Long id);
}
