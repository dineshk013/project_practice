package com.revcart.orderservice.controller;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.OrderDto;
import com.revcart.orderservice.dto.StatusUpdateRequest;
import com.revcart.orderservice.entity.Order;
import com.revcart.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    @GetMapping("/orders/assigned")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAssignedOrders(
            @RequestHeader("X-User-Id") Long userId) {
        List<OrderDto> orders = orderService.getOrdersByDeliveryAgent(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Assigned orders retrieved"));
    }
    
    @GetMapping("/orders/in-transit")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getInTransitOrders(
            @RequestHeader("X-User-Id") Long userId) {
        List<OrderDto> orders = orderService.getInTransitOrdersByAgent(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "In-transit orders retrieved"));
    }
    
    @GetMapping("/orders/pending")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getPendingOrders() {
        List<OrderDto> orders = orderService.getPendingDeliveryOrders();
        return ResponseEntity.ok(ApiResponse.success(orders, "Pending orders retrieved"));
    }

    @PostMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) StatusUpdateRequest request) {
        
        // Accept both query param and request body
        String statusStr = status != null ? status : 
                          (request != null ? request.getStatus() : null);
        
        if (statusStr == null || statusStr.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status is required"));
        }
        
        // Normalize to uppercase
        statusStr = statusStr.toUpperCase().trim();
        
        Order.OrderStatus orderStatus;
        try {
            orderStatus = Order.OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + statusStr));
        }
        
        OrderDto order = orderService.updateOrderStatus(orderId, orderStatus);
        return ResponseEntity.ok(ApiResponse.success(order, "Delivery status updated"));
    }
}
