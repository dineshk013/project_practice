package com.revcart.cartservice.client;

import com.revcart.cartservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    
    @PostMapping("/api/users/validate-token")
    ApiResponse<Boolean> validateToken(@RequestBody String token);
}
