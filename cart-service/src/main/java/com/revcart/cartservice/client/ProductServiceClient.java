package com.revcart.cartservice.client;

import com.revcart.cartservice.dto.ApiResponse;
import com.revcart.cartservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable Long id);
}
