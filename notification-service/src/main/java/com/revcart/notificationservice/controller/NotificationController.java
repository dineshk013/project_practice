package com.revcart.notificationservice.controller;

import com.revcart.notificationservice.dto.ApiResponse;
import com.revcart.notificationservice.dto.NotificationDto;
import com.revcart.notificationservice.dto.NotificationRequest;
import com.revcart.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<NotificationDto>> notifyOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam String eventType) {
        
        NotificationDto notification = switch (eventType.toUpperCase()) {
            case "PLACED" -> notificationService.notifyOrderPlaced(orderId, userId);
            case "SHIPPED" -> notificationService.notifyOrderShipped(orderId, userId);
            case "DELIVERED" -> notificationService.notifyOrderDelivered(orderId, userId);
            default -> throw new IllegalArgumentException("Invalid event type: " + eventType);
        };
        
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", notification));
    }

    @PostMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<NotificationDto>> notifyPayment(
            @PathVariable Long paymentId,
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        
        NotificationDto notification = status.equalsIgnoreCase("SUCCESS")
                ? notificationService.notifyPaymentSuccess(paymentId, userId, orderId)
                : notificationService.notifyPaymentFailed(paymentId, userId, orderId, reason);
        
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", notification));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDto>> createNotification(@RequestBody NotificationRequest request) {
        NotificationDto notification = notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Notification created successfully", notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationDto>> getNotificationById(@PathVariable Long id) {
        NotificationDto notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }
}
