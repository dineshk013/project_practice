package com.revcart.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.revcart.orderservice.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private String customerName;
    private UserInfo user;
    private String orderNumber;
    private Order.OrderStatus status;
    private Double totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Long deliveryAgentId;
    
    @JsonProperty("deliveryAddress")
    private AddressDto deliveryAddress;
    
    @JsonProperty("shippingAddress")
    public AddressDto getShippingAddress() {
        return deliveryAddress;
    }
    
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String fullName;
        private String email;
        private String phone;
    }
}
