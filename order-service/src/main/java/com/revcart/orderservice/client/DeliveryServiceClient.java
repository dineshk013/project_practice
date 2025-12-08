package com.revcart.orderservice.client;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.AssignDeliveryRequest;
import com.revcart.orderservice.dto.DeliveryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service", url = "${services.delivery-service.url}")
public interface DeliveryServiceClient {
    
    @PostMapping("/api/delivery/assign")
    ApiResponse<DeliveryDto> assignDelivery(@RequestBody AssignDeliveryRequest request);
}
