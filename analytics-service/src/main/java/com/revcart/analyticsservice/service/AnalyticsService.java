package com.revcart.analyticsservice.service;

import com.revcart.analyticsservice.client.OrderServiceClient;
import com.revcart.analyticsservice.client.ProductServiceClient;
import com.revcart.analyticsservice.client.UserServiceClient;
import com.revcart.analyticsservice.domain.entity.AnalyticsMetric;
import com.revcart.analyticsservice.dto.*;
import com.revcart.analyticsservice.exception.AnalyticsComputationException;
import com.revcart.analyticsservice.repository.AnalyticsMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsMetricRepository metricRepository;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @Scheduled(fixedRateString = "${analytics.cache.ttl-minutes:60}000", initialDelay = 10000)
    @Transactional
    public void refreshAnalytics() {
        log.info("Starting scheduled analytics refresh");
        try {
            computeOrderAnalytics();
            computeProductAnalytics();
            computeUserAnalytics();
            log.info("Analytics refresh completed successfully");
        } catch (Exception e) {
            log.error("Failed to refresh analytics: {}", e.getMessage(), e);
        }
    }

    public OrderAnalyticsDto computeOrderAnalytics() {
        try {
            List<Map<String, Object>> orders = orderServiceClient.getAllOrders();
            
            long totalOrders = orders.size();
            double totalRevenue = orders.stream()
                    .mapToDouble(o -> ((Number) o.getOrDefault("totalAmount", 0)).doubleValue())
                    .sum();
            double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
            
            LocalDateTime now = LocalDateTime.now();
            long ordersToday = orders.stream()
                    .filter(o -> isToday(o.get("createdAt")))
                    .count();
            long ordersThisWeek = orders.stream()
                    .filter(o -> isThisWeek(o.get("createdAt")))
                    .count();
            long ordersThisMonth = orders.stream()
                    .filter(o -> isThisMonth(o.get("createdAt")))
                    .count();

            saveMetric("total_orders", (double) totalOrders, AnalyticsMetric.Period.REALTIME);
            saveMetric("total_revenue", totalRevenue, AnalyticsMetric.Period.REALTIME);
            saveMetric("avg_order_value", avgOrderValue, AnalyticsMetric.Period.REALTIME);
            saveMetric("orders_today", (double) ordersToday, AnalyticsMetric.Period.DAILY);
            saveMetric("orders_this_week", (double) ordersThisWeek, AnalyticsMetric.Period.WEEKLY);
            saveMetric("orders_this_month", (double) ordersThisMonth, AnalyticsMetric.Period.MONTHLY);

            return new OrderAnalyticsDto(totalOrders, totalRevenue, avgOrderValue, 
                    ordersToday, ordersThisWeek, ordersThisMonth);
        } catch (Exception e) {
            log.error("Failed to compute order analytics", e);
            throw new AnalyticsComputationException("Failed to compute order analytics", e);
        }
    }

    public ProductAnalyticsDto computeProductAnalytics() {
        try {
            List<Map<String, Object>> products = productServiceClient.getAllProducts();
            
            List<ProductAnalyticsDto.ProductSummary> topProducts = products.stream()
                    .sorted(Comparator.comparingInt((Map<String, Object> p) -> 
                            ((Number) p.getOrDefault("stockQuantity", 0)).intValue()).reversed())
                    .limit(5)
                    .map(this::toProductSummary)
                    .collect(Collectors.toList());
            
            List<ProductAnalyticsDto.ProductSummary> lowStockProducts = products.stream()
                    .filter(p -> ((Number) p.getOrDefault("stockQuantity", 0)).intValue() < 10)
                    .map(this::toProductSummary)
                    .collect(Collectors.toList());

            saveMetric("total_products", (double) products.size(), AnalyticsMetric.Period.REALTIME);

            return new ProductAnalyticsDto(topProducts, lowStockProducts, (long) products.size());
        } catch (Exception e) {
            log.error("Failed to compute product analytics", e);
            throw new AnalyticsComputationException("Failed to compute product analytics", e);
        }
    }

    public UserAnalyticsDto computeUserAnalytics() {
        try {
            List<Map<String, Object>> users = userServiceClient.getAllUsers();
            
            long totalUsers = users.size();
            long newUsersThisMonth = users.stream()
                    .filter(u -> isThisMonth(u.get("createdAt")))
                    .count();
            long returningCustomers = totalUsers - newUsersThisMonth;

            saveMetric("total_users", (double) totalUsers, AnalyticsMetric.Period.REALTIME);
            saveMetric("new_users_this_month", (double) newUsersThisMonth, AnalyticsMetric.Period.MONTHLY);
            saveMetric("returning_customers", (double) returningCustomers, AnalyticsMetric.Period.REALTIME);

            return new UserAnalyticsDto(totalUsers, newUsersThisMonth, returningCustomers);
        } catch (Exception e) {
            log.error("Failed to compute user analytics", e);
            throw new AnalyticsComputationException("Failed to compute user analytics", e);
        }
    }

    public DashboardDto computeDashboardData() {
        return new DashboardDto(
                computeOrderAnalytics(),
                computeProductAnalytics(),
                computeUserAnalytics()
        );
    }

    public List<ProductAnalyticsDto.ProductSummary> getTopProducts() {
        try {
            List<Map<String, Object>> products = productServiceClient.getAllProducts();
            return products.stream()
                    .sorted(Comparator.comparingInt((Map<String, Object> p) -> 
                            ((Number) p.getOrDefault("stockQuantity", 0)).intValue()).reversed())
                    .limit(5)
                    .map(this::toProductSummary)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get top products", e);
            throw new AnalyticsComputationException("Failed to get top products", e);
        }
    }

    public List<ProductAnalyticsDto.ProductSummary> getLowStockProducts() {
        try {
            List<Map<String, Object>> products = productServiceClient.getAllProducts();
            return products.stream()
                    .filter(p -> ((Number) p.getOrDefault("stockQuantity", 0)).intValue() < 10)
                    .map(this::toProductSummary)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get low stock products", e);
            throw new AnalyticsComputationException("Failed to get low stock products", e);
        }
    }

    private void saveMetric(String name, Double value, AnalyticsMetric.Period period) {
        AnalyticsMetric metric = metricRepository.findByMetricNameAndPeriod(name, period)
                .orElse(new AnalyticsMetric());
        metric.setMetricName(name);
        metric.setMetricValue(value);
        metric.setPeriod(period);
        metricRepository.save(metric);
    }

    private ProductAnalyticsDto.ProductSummary toProductSummary(Map<String, Object> p) {
        return new ProductAnalyticsDto.ProductSummary(
                ((Number) p.get("id")).longValue(),
                (String) p.get("name"),
                ((Number) p.getOrDefault("stockQuantity", 0)).intValue(),
                ((Number) p.getOrDefault("price", 0.0)).doubleValue()
        );
    }

    private boolean isToday(Object timestamp) {
        if (timestamp == null) return false;
        LocalDateTime date = parseTimestamp(timestamp);
        return date.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    private boolean isThisWeek(Object timestamp) {
        if (timestamp == null) return false;
        LocalDateTime date = parseTimestamp(timestamp);
        return date.isAfter(LocalDateTime.now().minusWeeks(1));
    }

    private boolean isThisMonth(Object timestamp) {
        if (timestamp == null) return false;
        LocalDateTime date = parseTimestamp(timestamp);
        return date.isAfter(LocalDateTime.now().minusMonths(1));
    }

    private LocalDateTime parseTimestamp(Object timestamp) {
        if (timestamp == null) return LocalDateTime.now();
        if (timestamp instanceof LocalDateTime) {
            return (LocalDateTime) timestamp;
        }
        if (timestamp instanceof String) {
            try {
                return LocalDateTime.parse((String) timestamp);
            } catch (Exception e) {
                log.warn("Failed to parse timestamp: {}", timestamp);
            }
        }
        if (timestamp instanceof java.util.List) {
            try {
                @SuppressWarnings("unchecked")
                java.util.List<Integer> parts = (java.util.List<Integer>) timestamp;
                if (parts.size() >= 3) {
                    return LocalDateTime.of(
                        parts.get(0), parts.get(1), parts.get(2),
                        parts.size() > 3 ? parts.get(3) : 0,
                        parts.size() > 4 ? parts.get(4) : 0,
                        parts.size() > 5 ? parts.get(5) : 0
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to parse timestamp array: {}", timestamp);
            }
        }
        return LocalDateTime.now();
    }
}
