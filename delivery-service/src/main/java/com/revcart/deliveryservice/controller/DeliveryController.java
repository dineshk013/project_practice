package com.revcart.deliveryservice.controller;

import com.revcart.deliveryservice.dto.*;
import com.revcart.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<DeliveryDto>> assignDelivery(@RequestBody AssignDeliveryRequest request) {
        DeliveryDto delivery = deliveryService.assignDelivery(request);
        return ResponseEntity.ok(ApiResponse.success("Delivery assigned successfully", delivery));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateStatusRequest request) {
        DeliveryDto delivery = deliveryService.updateStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated successfully", delivery));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<DeliveryDto>> getDelivery(@PathVariable Long orderId) {
        DeliveryDto delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Delivery retrieved successfully", delivery));
    }

    @GetMapping("/{orderId}/track")
    public ResponseEntity<ApiResponse<List<TrackingLogDto>>> getTrackingHistory(@PathVariable Long orderId) {
        List<TrackingLogDto> trackingLogs = deliveryService.getTrackingHistory(orderId);
        return ResponseEntity.ok(ApiResponse.success("Tracking history retrieved successfully", trackingLogs));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getUserDeliveries(@PathVariable Long userId) {
        List<DeliveryDto> deliveries = deliveryService.getUserDeliveries(userId);
        return ResponseEntity.ok(ApiResponse.success("User deliveries retrieved successfully", deliveries));
    }
}
