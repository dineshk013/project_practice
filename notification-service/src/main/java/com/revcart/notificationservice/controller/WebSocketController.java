package com.revcart.notificationservice.controller;

import com.revcart.notificationservice.dto.NotificationDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(Long userId, NotificationDto notification) {
        messagingTemplate.convertAndSend("/topic/orders/" + userId, notification);
    }

    @MessageMapping("/notification")
    @SendTo("/topic/notifications")
    public NotificationDto sendNotification(NotificationDto notification) {
        return notification;
    }
}
