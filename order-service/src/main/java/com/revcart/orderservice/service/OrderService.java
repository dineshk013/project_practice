package com.revcart.orderservice.service;

import com.revcart.orderservice.client.CartServiceClient;
import com.revcart.orderservice.client.DeliveryServiceClient;
import com.revcart.orderservice.client.NotificationServiceClient;
import com.revcart.orderservice.client.PaymentServiceClient;
import com.revcart.orderservice.client.ProductServiceClient;
import com.revcart.orderservice.client.UserServiceClient;
import com.revcart.orderservice.dto.*;
import com.revcart.orderservice.entity.DeliveryAddress;
import com.revcart.orderservice.entity.Order;
import com.revcart.orderservice.entity.OrderItem;
import com.revcart.orderservice.exception.BadRequestException;
import com.revcart.orderservice.exception.ResourceNotFoundException;
import com.revcart.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final DeliveryServiceClient deliveryServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    /**
     * NOTE: No @Transactional here so that the saved order is committed
     * before external services (payment/delivery) call back into this service.
     */
    public OrderDto checkout(Long userId, CheckoutRequest request) {
        log.info("=== CHECKOUT START === userId: {}, addressId: {}, paymentMethod: {}",
                userId, request.getAddressId(), request.getPaymentMethod());

        // 1. Validate userId header
        if (userId == null) {
            log.error("UserId is null in checkout request");
            throw new BadRequestException("UserId missing in request");
        }

        // 2. Get and validate cart
        log.info("Fetching cart for userId: {}", userId);
        ApiResponse<CartDto> cartResponse = cartServiceClient.getCart(userId);
        if (!cartResponse.isSuccess() || cartResponse.getData() == null) {
            log.error("Cart is empty or fetch failed for userId: {}", userId);
            throw new BadRequestException("Cart is empty");
        }
        CartDto cart = cartResponse.getData();
        log.info("Cart fetched: {} items, total: {}", cart.getItems().size(), cart.getTotalPrice());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.error("Cart has no items for userId: {}", userId);
            throw new BadRequestException("Cart is empty");
        }

        // 3. Validate cart items (non-blocking)
        try {
            ApiResponse<Boolean> validationResponse = cartServiceClient.validateCart(userId);
            if (!validationResponse.isSuccess() || !Boolean.TRUE.equals(validationResponse.getData())) {
                log.warn("Cart validation failed for userId: {}, continuing anyway", userId);
            }
        } catch (Exception e) {
            log.warn("Cart validation threw exception, continuing: {}", e.getMessage());
        }

        // 4. Get delivery address
        log.info("Fetching address: {}", request.getAddressId());
        ApiResponse<List<AddressDto>> addressResponse = userServiceClient.getAddresses(userId);
        AddressDto address = addressResponse.getData().stream()
                .filter(a -> a.getId().equals(request.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Address not found"));
        log.info("Address found: {}, {}", address.getCity(), address.getState());

        // 5. Reserve stock (non-blocking)
        try {
            StockReservationRequest stockRequest = new StockReservationRequest();
            stockRequest.setReservationId("ORD-" + System.currentTimeMillis());
            stockRequest.setItems(cart.getItems().stream()
                    .map(item -> new StockReservationRequest.StockItem(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList()));
            productServiceClient.reserveStock(stockRequest);
            log.info("Stock reserved successfully");
        } catch (Exception e) {
            log.warn("Stock reservation failed, continuing with order: {}", e.getMessage());
        }

        // 6. Create order entity
        log.info("Creating order entity for userId: {}", userId);
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(cart.getTotalPrice());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setStreet(address.getStreet());
        deliveryAddress.setCity(address.getCity());
        deliveryAddress.setState(address.getState());
        deliveryAddress.setZipCode(address.getZipCode());
        deliveryAddress.setCountry(address.getCountry());
        order.setDeliveryAddress(deliveryAddress);

        // Add order items
        for (CartItemDto cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setImageUrl(cartItem.getImageUrl());
            order.getItems().add(orderItem);
        }
        log.info("Order entity created with {} items", order.getItems().size());

        // 7. SAVE ORDER TO DATABASE
        log.info("Saving order to database...");
        Order saved = orderRepository.save(order);
        log.info("=== ORDER SAVED === ID: {}, OrderNumber: {}", saved.getId(), saved.getOrderNumber());

        // Verify save
        boolean exists = orderRepository.existsById(saved.getId());
        log.info("Order exists in DB after save: {}", exists);
        if (!exists) {
            log.error("ORDER NOT FOUND IN DB AFTER SAVE! This should never happen.");
        }

        // 8. Handle COD orders - mark as confirmed immediately
        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            saved.setPaymentStatus(Order.PaymentStatus.COD);
            saved.setStatus(Order.OrderStatus.PAYMENT_SUCCESS);
            orderRepository.save(saved);
            log.info("COD order confirmed for order: {}", saved.getOrderNumber());
            
            // Send confirmation notification for COD
            sendOrderNotification(saved.getId(), userId, "CONFIRMED");
        }

        // 9-12. Post-order operations (non-blocking, external services)
        performPostOrderOperations(saved, userId, request.getPaymentMethod());

        log.info("=== CHECKOUT COMPLETE === OrderID: {}, OrderNumber: {}", saved.getId(), saved.getOrderNumber());
        return toDto(saved);
    }

    private void performPostOrderOperations(Order order, Long userId, String paymentMethod) {
        boolean paymentSuccess = true;
        boolean deliverySuccess = true;
        boolean notificationSuccess = true;

        // Initiate payment (non-blocking)
        if (!"COD".equalsIgnoreCase(paymentMethod)) {
            try {
                PaymentInitiateRequest paymentRequest = new PaymentInitiateRequest(
                        order.getId(),
                        userId,
                        order.getTotalAmount(),
                        paymentMethod
                );
                ApiResponse<PaymentDto> paymentResponse = paymentServiceClient.initiatePayment(paymentRequest);
                log.info("Payment initiated for order: {}", order.getId());
            } catch (Exception e) {
                paymentSuccess = false;
                log.error("Payment initiation failed for order: {}, error: {}", order.getId(), e.getMessage());
            }
        }

        // Assign delivery (non-blocking)
        try {
            AssignDeliveryRequest deliveryRequest = new AssignDeliveryRequest(
                    order.getId(),
                    userId,
                    null,
                    java.time.LocalDateTime.now().plusDays(3)
            );
            deliveryServiceClient.assignDelivery(deliveryRequest);
            log.info("Delivery assigned for order: {}", order.getId());
        } catch (Exception e) {
            deliverySuccess = false;
            log.error("Delivery assignment failed for order: {}, error: {}", order.getId(), e.getMessage());
        }

        // Note: Notification is sent only after payment success or for COD orders

        // Clear cart
        try {
            cartServiceClient.clearCart(userId);
            log.info("✅ Cart cleared for userId: {} after order completion", userId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to clear cart for userId: {}, error: {}", userId, e.getMessage());
        }

        // Flags currently only logged; you can add extra handling if needed
        if (!paymentSuccess) {
            log.warn("Payment may have failed for order: {}", order.getId());
        }
        if (!deliverySuccess) {
            log.warn("Delivery assignment may have failed for order: {}", order.getId());
        }
        if (!notificationSuccess) {
            log.warn("Notification may have failed for order: {}", order.getId());
        }
    }

    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toDto(order);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, Order.OrderStatus status) {
        log.info("Updating order status: orderId={}, newStatus={}", id, status);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        log.info("Current order status: {}, userId: {}", order.getStatus(), order.getUserId());
        
        order.setStatus(status);
        
        // Auto-assign delivery agent when status changes to OUT_FOR_DELIVERY
        if (status == Order.OrderStatus.OUT_FOR_DELIVERY && order.getDeliveryAgentId() == null) {
            try {
                ApiResponse<java.util.List<Object>> agentsResponse = userServiceClient.getDeliveryAgents();
                if (agentsResponse.isSuccess() && agentsResponse.getData() != null && !agentsResponse.getData().isEmpty()) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> firstAgent = (java.util.Map<String, Object>) agentsResponse.getData().get(0);
                    Long agentId = ((Number) firstAgent.get("id")).longValue();
                    order.setDeliveryAgentId(agentId);
                    log.info("Auto-assigned delivery agent {} to order {}", agentId, id);
                }
            } catch (Exception e) {
                log.warn("Failed to auto-assign delivery agent for order {}: {}", id, e.getMessage());
            }
        }
        
        try {
            Order updated = orderRepository.save(order);
            log.info("Order status updated successfully: {} -> {}", id, status);
            
            // Send notification for status changes
            if (status == Order.OrderStatus.SHIPPED || status == Order.OrderStatus.OUT_FOR_DELIVERY) {
                sendOrderNotification(id, order.getUserId(), "SHIPPED");
            } else if (status == Order.OrderStatus.DELIVERED) {
                sendOrderNotification(id, order.getUserId(), "DELIVERED");
            }
            
            return toDto(updated);
        } catch (Exception e) {
            log.error("Failed to update order status: orderId={}, status={}, error={}", id, status, e.getMessage(), e);
            throw new RuntimeException("Failed to update order status: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void cancelOrder(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized to cancel this order");
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED ||
                order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel order in current status");
        }

        // Release stock
        StockReservationRequest stockRequest = new StockReservationRequest();
        stockRequest.setReservationId(order.getOrderNumber());
        stockRequest.setItems(order.getItems().stream()
                .map(item -> new StockReservationRequest.StockItem(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList()));

        try {
            productServiceClient.releaseStock(stockRequest);
        } catch (Exception e) {
            log.error("Failed to release stock: {}", e.getMessage());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Send order cancelled notification
        sendOrderNotification(id, userId, "CANCELLED");

        log.info("Order cancelled: {}", id);
    }

    public boolean validateOrder(Long orderId) {
        return orderRepository.existsById(orderId);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<OrderDto> getAllOrdersPaged(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toDto);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. Total orders count
        long totalOrders = orderRepository.count();
        
        // 2. Total revenue (only COMPLETED or DELIVERED orders)
        double totalRevenue = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.COMPLETED || 
                                order.getStatus() == Order.OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
        
        // 3. Total products from product service
        long totalProducts = 0;
        try {
            ApiResponse<Object> productResponse = productServiceClient.getAllProducts();
            if (productResponse.isSuccess() && productResponse.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> products = (java.util.List<Object>) productResponse.getData();
                totalProducts = products.size();
            }
        } catch (Exception e) {
            log.error("Failed to fetch products: {}", e.getMessage());
        }
        
        // 4. Active users and total users from user service
        long activeUsers = 0;
        long totalUsers = 0;
        try {
            Long activeUsersCount = userServiceClient.getActiveUsersCount();
            if (activeUsersCount != null) {
                activeUsers = activeUsersCount;
            }
            
            ApiResponse<Object> usersResponse = userServiceClient.getAllUsers();
            if (usersResponse.isSuccess() && usersResponse.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.List<java.util.Map<String, Object>> users = (java.util.List<java.util.Map<String, Object>>) usersResponse.getData();
                totalUsers = users.size();
            }
        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage());
        }

        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalProducts", totalProducts);
        stats.put("activeUsers", activeUsers);
        stats.put("totalUsers", totalUsers);

        return stats;
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentStatus(mapPaymentStatusForFrontend(order.getPaymentStatus()));
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setDeliveryAgentId(order.getDeliveryAgentId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Fetch customer info
        try {
            ApiResponse<Object> userResponse = userServiceClient.getUserById(order.getUserId());
            if (userResponse.isSuccess() && userResponse.getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> userData = (java.util.Map<String, Object>) userResponse.getData();
                String name = (String) userData.get("name");
                dto.setCustomerName(name);
                
                OrderDto.UserInfo userInfo = new OrderDto.UserInfo();
                userInfo.setFullName(name);
                userInfo.setEmail((String) userData.get("email"));
                userInfo.setPhone((String) userData.get("phone"));
                dto.setUser(userInfo);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch customer info for order {}: {}", order.getId(), e.getMessage());
            dto.setCustomerName("N/A");
        }

        if (order.getDeliveryAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setStreet(order.getDeliveryAddress().getStreet());
            addressDto.setCity(order.getDeliveryAddress().getCity());
            addressDto.setState(order.getDeliveryAddress().getState());
            addressDto.setZipCode(order.getDeliveryAddress().getZipCode());
            addressDto.setCountry(order.getDeliveryAddress().getCountry());
            dto.setDeliveryAddress(addressDto);
        }

        dto.setItems(order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getImageUrl()
                ))
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void updatePaymentStatus(Long orderId, String status) {
        log.info("Updating payment status for order {}: {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        try {
            // Handle PAYMENT_SUCCESS status
            if ("PAYMENT_SUCCESS".equalsIgnoreCase(status)) {
                order.setStatus(Order.OrderStatus.PAYMENT_SUCCESS);
                order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
                orderRepository.save(order);
                log.info("Order marked as PAYMENT_SUCCESS for order {}", orderId);
                
                // Send order confirmation notification
                sendOrderNotification(orderId, order.getUserId(), "CONFIRMED");
                return;
            }
            
            // Handle other payment statuses
            Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status.toUpperCase());
            order.setPaymentStatus(paymentStatus);

            if (paymentStatus == Order.PaymentStatus.COMPLETED) {
                order.setStatus(Order.OrderStatus.CONFIRMED);
            }

            orderRepository.save(order);
            log.info("Payment status updated successfully for order {}: {}", orderId, status);
        } catch (Exception e) {
            log.error("Failed to update payment status for order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to update payment status: " + e.getMessage(), e);
        }
    }

    private void sendOrderNotification(Long orderId, Long userId, String eventType) {
        try {
            notificationServiceClient.notifyOrder(orderId, userId, eventType);
            log.info("Notification sent for order: {}, event: {}", orderId, eventType);
        } catch (Exception e) {
            log.error("Failed to send notification for order: {}, event: {}. Error: {}",
                    orderId, eventType, e.getMessage());
        }
    }

    private String mapPaymentStatusForFrontend(Order.PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            return "PENDING";
        }
        return switch (paymentStatus) {
            case COMPLETED -> "SUCCESS";
            case PENDING -> "PENDING";
            case FAILED -> "FAILED";
            case REFUNDED -> "REFUNDED";
            case COD -> "COD";
        };
    }
    
    public List<OrderDto> getOrdersByDeliveryAgent(Long agentId) {
        return orderRepository.findByDeliveryAgentId(agentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getInTransitOrdersByAgent(Long agentId) {
        return orderRepository.findByDeliveryAgentId(agentId).stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.OUT_FOR_DELIVERY)
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getPendingDeliveryOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getDeliveryAgentId() == null && order.getStatus() == Order.OrderStatus.PACKED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
