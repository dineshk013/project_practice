package com.revcart.orderservice.client;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.PaymentDto;
import com.revcart.orderservice.dto.PaymentInitiateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${services.payment-service.url}")
public interface PaymentServiceClient {
    
    @PostMapping("/api/payments/initiate")
    ApiResponse<PaymentDto> initiatePayment(@RequestBody PaymentInitiateRequest request);
}
