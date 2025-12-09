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

    // --------- USED BY ADMIN / OTHER SERVICES ----------

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<DeliveryDto>> assignDelivery(@RequestBody AssignDeliveryRequest request) {
        DeliveryDto delivery = deliveryService.assignDelivery(request);
        return ResponseEntity.ok(ApiResponse.success("Delivery assigned successfully", delivery));
    }

    /**
     * Original status update endpoint (kept for compatibility)
     * PUT /api/delivery/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateStatusRequest request) {
        DeliveryDto delivery = deliveryService.updateStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated successfully", delivery));
    }

    /**
     * Frontend delivery dashboard calls:
     * POST /api/delivery/orders/{orderId}/status
     */
    @PostMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<DeliveryDto>> updateStatusFromDashboard(
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

    // --------- ENDPOINTS USED BY DELIVERY DASHBOARD (FRONTEND) ----------

    /**
     * Assigned deliveries for the logged-in delivery agent
     * Frontend: GET /api/delivery/orders/assigned with header X-User-Id
     */
    @GetMapping("/orders/assigned")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getAssignedOrders(
            @RequestHeader("X-User-Id") Long agentId) {

        List<DeliveryDto> deliveries = deliveryService.getAssignedDeliveriesForAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success("Assigned deliveries retrieved successfully", deliveries));
    }

    /**
     * In-transit deliveries for the logged-in delivery agent
     * Frontend: GET /api/delivery/orders/in-transit with header X-User-Id
     */
    @GetMapping("/orders/in-transit")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getInTransitOrders(
            @RequestHeader("X-User-Id") Long agentId) {

        List<DeliveryDto> deliveries = deliveryService.getInTransitDeliveriesForAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success("In-transit deliveries retrieved successfully", deliveries));
    }

    /**
     * Pending deliveries for the logged-in delivery agent
     * (e.g. assigned but not yet started)
     * Frontend: GET /api/delivery/orders/pending with header X-User-Id
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<ApiResponse<List<DeliveryDto>>> getPendingOrders(
            @RequestHeader("X-User-Id") Long agentId) {

        List<DeliveryDto> deliveries = deliveryService.getPendingDeliveriesForAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success("Pending deliveries retrieved successfully", deliveries));
    }
}
