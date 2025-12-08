package com.revcart.notificationservice.service;

import com.revcart.notificationservice.controller.WebSocketController;
import com.revcart.notificationservice.dto.NotificationDto;
import com.revcart.notificationservice.dto.NotificationRequest;
import com.revcart.notificationservice.entity.Notification;
import com.revcart.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketController webSocketController;

    public NotificationDto createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: {}", saved.getId());
        
        NotificationDto dto = NotificationDto.fromEntity(saved);
        webSocketController.sendNotificationToUser(request.getUserId(), dto);
        
        return dto;
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public NotificationDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(String.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return NotificationDto.fromEntity(notification);
    }

    public NotificationDto notifyOrderPlaced(Long orderId, Long userId) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType("ORDER_PLACED");
        request.setMessage("Your order #" + orderId + " has been placed successfully");
        return createNotification(request);
    }

    public NotificationDto notifyOrderShipped(Long orderId, Long userId) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType("ORDER_SHIPPED");
        request.setMessage("Your order #" + orderId + " has been shipped");
        return createNotification(request);
    }

    public NotificationDto notifyOrderDelivered(Long orderId, Long userId) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType("ORDER_DELIVERED");
        request.setMessage("Your order #" + orderId + " has been delivered");
        return createNotification(request);
    }

    public NotificationDto notifyPaymentSuccess(Long paymentId, Long userId, Long orderId) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType("PAYMENT_SUCCESS");
        request.setMessage("Payment successful for order #" + orderId);
        return createNotification(request);
    }

    public NotificationDto notifyPaymentFailed(Long paymentId, Long userId, Long orderId, String reason) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType("PAYMENT_FAILED");
        request.setMessage("Payment failed for order #" + orderId + ". Reason: " + reason);
        return createNotification(request);
    }
}
