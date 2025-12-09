package com.revcart.deliveryservice.repository;

import com.revcart.deliveryservice.entity.Delivery;
import com.revcart.deliveryservice.entity.Delivery.DeliveryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, String> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByOrderId(Long orderId);

    // New methods for delivery dashboard

    // All deliveries assigned to a specific delivery agent
    List<Delivery> findByAgentIdOrderByCreatedAtDesc(Long agentId);

    // Deliveries for an agent filtered by a list of statuses
    List<Delivery> findByAgentIdAndStatusInOrderByCreatedAtDesc(Long agentId, List<DeliveryStatus> statuses);

    // Deliveries for an agent with a single status
    List<Delivery> findByAgentIdAndStatusOrderByCreatedAtDesc(Long agentId, DeliveryStatus status);
}
