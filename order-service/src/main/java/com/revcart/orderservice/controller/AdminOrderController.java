package com.revcart.orderservice.controller;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.OrderDto;
import com.revcart.orderservice.entity.Order;
import com.revcart.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getAdminOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderDto> orderPage = orderService.getAllOrdersPaged(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", orderPage.getContent());
        response.put("page", orderPage.getNumber());
        response.put("size", orderPage.getSize());
        response.put("totalElements", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
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
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = orderService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
