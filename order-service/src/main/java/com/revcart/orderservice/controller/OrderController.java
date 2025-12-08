package com.revcart.orderservice.controller;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.CheckoutRequest;
import com.revcart.orderservice.dto.OrderDto;
import com.revcart.orderservice.entity.Order;
import com.revcart.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CheckoutRequest request) {
        OrderDto order = orderService.checkout(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order placed successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(@RequestHeader("X-User-Id") Long userId) {
        List<OrderDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrdersAlt(@RequestHeader("X-User-Id") Long userId) {
        List<OrderDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        OrderDto order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Order cancelled successfully"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateOrder(@RequestParam Long orderId) {
        boolean valid = orderService.validateOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(valid, "Order validation completed"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders, "All orders retrieved"));
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<Void>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        orderService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment status updated"));
    }
}
