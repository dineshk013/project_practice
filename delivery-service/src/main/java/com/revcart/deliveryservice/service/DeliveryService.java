package com.revcart.deliveryservice.service;

import com.revcart.deliveryservice.client.NotificationServiceClient;
import com.revcart.deliveryservice.client.OrderServiceClient;
import com.revcart.deliveryservice.dto.*;
import com.revcart.deliveryservice.entity.Delivery;
import com.revcart.deliveryservice.entity.Delivery.DeliveryStatus;
import com.revcart.deliveryservice.entity.DeliveryTrackingLog;
import com.revcart.deliveryservice.exception.BadRequestException;
import com.revcart.deliveryservice.exception.ResourceNotFoundException;
import com.revcart.deliveryservice.repository.DeliveryRepository;
import com.revcart.deliveryservice.repository.DeliveryTrackingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryTrackingLogRepository trackingLogRepository;
    private final OrderServiceClient orderServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // ---------------- CORE OPERATIONS ----------------

    @Transactional
    public DeliveryDto assignDelivery(AssignDeliveryRequest request) {
        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            throw new BadRequestException("Delivery already assigned for order: " + request.getOrderId());
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(request.getOrderId());
        delivery.setUserId(request.getUserId());
        delivery.setAgentId(request.getAgentId());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setUpdatedAt(LocalDateTime.now());

        delivery = deliveryRepository.save(delivery);

        addTrackingLog(delivery, DeliveryStatus.ASSIGNED, null, "Delivery agent assigned");

        // Notify customer that a delivery agent has been assigned
        sendNotification(request.getOrderId(), request.getUserId(), "ASSIGNED");

        log.info("Delivery assigned: {} for order: {}", delivery.getId(), request.getOrderId());
        return DeliveryDto.fromEntity(delivery);
    }

    @Transactional
    public DeliveryDto updateStatus(Long orderId, UpdateStatusRequest request) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order: " + orderId));

        DeliveryStatus newStatus = DeliveryStatus.valueOf(request.getStatus());
        delivery.setStatus(newStatus);
        delivery.setUpdatedAt(LocalDateTime.now());

        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setActualDeliveryDate(LocalDateTime.now());
            notifyOrderService(orderId, "DELIVERED");
            sendNotification(orderId, delivery.getUserId(), "DELIVERED");
        }

        delivery = deliveryRepository.save(delivery);
        addTrackingLog(delivery, newStatus, request.getLocation(), request.getMessage());

        log.info("Delivery status updated: order={}, status={}", orderId, newStatus);
        return DeliveryDto.fromEntity(delivery);
    }

    public DeliveryDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order: " + orderId));
        return DeliveryDto.fromEntity(delivery);
    }

    public List<TrackingLogDto> getTrackingHistory(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order: " + orderId));

        return trackingLogRepository.findByDeliveryIdOrderByTimestampDesc(delivery.getId())
                .stream()
                .map(TrackingLogDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DeliveryDto> getUserDeliveries(Long userId) {
        return deliveryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ---------------- DASHBOARD QUERIES (USED BY /orders/* ENDPOINTS) ----------------

    /**
     * Assigned deliveries for agent (anything not yet delivered/cancelled)
     */
    public List<DeliveryDto> getAssignedDeliveriesForAgent(Long agentId) {
        // ASSIGNED + PICKED_UP + IN_TRANSIT + OUT_FOR_DELIVERY
        List<DeliveryStatus> statuses = List.of(
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP,
                DeliveryStatus.IN_TRANSIT,
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        return deliveryRepository.findByAgentIdAndStatusInOrderByCreatedAtDesc(agentId, statuses)
                .stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * In-transit deliveries for agent
     */
    public List<DeliveryDto> getInTransitDeliveriesForAgent(Long agentId) {
        List<DeliveryStatus> statuses = List.of(
                DeliveryStatus.IN_TRANSIT,
                DeliveryStatus.OUT_FOR_DELIVERY
        );

        return deliveryRepository.findByAgentIdAndStatusInOrderByCreatedAtDesc(agentId, statuses)
                .stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Pending deliveries for agent (assigned but not started)
     */
    public List<DeliveryDto> getPendingDeliveriesForAgent(Long agentId) {
        return deliveryRepository.findByAgentIdAndStatusOrderByCreatedAtDesc(agentId, DeliveryStatus.ASSIGNED)
                .stream()
                .map(DeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ---------------- INTERNAL HELPERS ----------------

    private void addTrackingLog(Delivery delivery, DeliveryStatus status, String location, String message) {
        DeliveryTrackingLog log = new DeliveryTrackingLog();
        log.setDelivery(delivery);
        log.setStatus(status);
        log.setLocation(location);
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        trackingLogRepository.save(log);
    }

    private void notifyOrderService(Long orderId, String status) {
        try {
            orderServiceClient.updateOrderStatus(orderId, status);
            log.info("Order service notified: order={}, status={}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to notify order service: {}", e.getMessage());
        }
    }

    private void sendNotification(Long orderId, Long userId, String eventType) {
        try {
            notificationServiceClient.notifyOrder(orderId, userId, eventType);
            log.info("Notification sent: order={}, event={}", orderId, eventType);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}
