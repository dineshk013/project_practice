package com.revcart.orderservice.controller;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.OrderDto;
import com.revcart.orderservice.entity.Order;
import com.revcart.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getDeliveryOrders(
            @RequestParam(required = false) Long agentId) {
        
        // For now, return all orders that are ready for delivery
        // In production, filter by agentId and delivery status
        List<OrderDto> orders = orderService.getAllOrders();
        
        return ResponseEntity.ok(ApiResponse.success(orders, "Delivery orders retrieved"));
    }

    @PostMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        
        String statusStr = request.get("status");
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + statusStr));
        }
        
        OrderDto order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success(order, "Delivery status updated"));
    }
}
