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
    private String orderNumber;
    private Order.OrderStatus status;
    private Double totalAmount;
    private Order.PaymentStatus paymentStatus;
    private String paymentMethod;
    
    @JsonProperty("deliveryAddress")
    private AddressDto deliveryAddress;
    
    @JsonProperty("shippingAddress")
    public AddressDto getShippingAddress() {
        return deliveryAddress;
    }
    
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
