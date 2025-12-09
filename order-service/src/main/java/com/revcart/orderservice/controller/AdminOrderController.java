package com.revcart.orderservice.controller;

import com.revcart.orderservice.dto.ApiResponse;
import com.revcart.orderservice.dto.OrderDto;
import com.revcart.orderservice.dto.StatusUpdateRequest;
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
            @RequestParam(required = false) String status,
            @RequestParam(name = "newStatus", required = false) String newStatus,
            @RequestParam(name = "orderStatus", required = false) String orderStatusParam,
            @RequestBody(required = false) StatusUpdateRequest request) {

        // 1. Gather status from all possible places
        String statusStr = null;

        if (status != null && !status.isBlank()) {
            statusStr = status;
        } else if (newStatus != null && !newStatus.isBlank()) {
            statusStr = newStatus;
        } else if (orderStatusParam != null && !orderStatusParam.isBlank()) {
            statusStr = orderStatusParam;
        } else if (request != null) {
            statusStr = request.resolveStatus();
        }

        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status is required"));
        }

        // 2. Normalize
        statusStr = statusStr.trim().toUpperCase();

        // 3. Map UI labels → enum values
        statusStr = switch (statusStr) {
            case "PROCESSING" -> "PACKED";              // UI: processing -> Backend: PACKED
            case "PACKED" -> "OUT_FOR_DELIVERY";       // UI: packed -> Backend: OUT_FOR_DELIVERY
            case "IN_TRANSIT", "IN TRANSIT" -> "DELIVERED";  // UI: in_transit -> Backend: DELIVERED
            case "DELIVERED" -> "COMPLETED";           // UI: delivered -> Backend: COMPLETED
            case "CANCELLED", "CANCELED" -> "CANCELLED";
            case "PENDING", "PLACED" -> "PENDING";
            case "CONFIRMED" -> "CONFIRMED";
            case "OUT_FOR_DELIVERY", "OUT FOR DELIVERY" -> "OUT_FOR_DELIVERY";
            case "COMPLETED" -> "COMPLETED";
            case "SHIPPED" -> "OUT_FOR_DELIVERY";      // Legacy support
            default -> statusStr;
        };

        // 4. Convert to enum
        Order.OrderStatus orderStatusEnum;
        try {
            orderStatusEnum = Order.OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + statusStr));
        }

        // 5. Update
        OrderDto order = orderService.updateOrderStatus(orderId, orderStatusEnum);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }


    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDto> getAdminOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = orderService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved successfully"));
    }
}
