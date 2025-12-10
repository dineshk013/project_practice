package com.revcart.orderservice.repository;

import com.revcart.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByDeliveryAgentId(Long agentId);
    List<Order> findByDeliveryAgentIdAndStatus(Long agentId, Order.OrderStatus status);
    List<Order> findByStatusIn(List<Order.OrderStatus> statuses);
    
    @Query("SELECT DISTINCT o.userId FROM Order o")
    List<Long> findDistinctUserIds();
}

