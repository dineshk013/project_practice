package com.revcart.deliveryservice.repository;

import com.revcart.deliveryservice.entity.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends MongoRepository<Delivery, String> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByOrderId(Long orderId);
}
