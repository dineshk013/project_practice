package com.revcart.deliveryservice.repository;

import com.revcart.deliveryservice.entity.DeliveryTrackingLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryTrackingLogRepository extends MongoRepository<DeliveryTrackingLog, String> {
    List<DeliveryTrackingLog> findByDeliveryIdOrderByTimestampDesc(String deliveryId);
}
