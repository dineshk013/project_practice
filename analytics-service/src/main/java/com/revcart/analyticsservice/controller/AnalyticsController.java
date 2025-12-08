package com.revcart.analyticsservice.controller;

import com.revcart.analyticsservice.dto.*;
import com.revcart.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/orders/summary")
    public ResponseEntity<ApiResponse<OrderAnalyticsDto>> getOrderSummary() {
        OrderAnalyticsDto analytics = analyticsService.computeOrderAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/products/summary")
    public ResponseEntity<ApiResponse<ProductAnalyticsDto>> getProductSummary() {
        ProductAnalyticsDto analytics = analyticsService.computeProductAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/users/summary")
    public ResponseEntity<ApiResponse<UserAnalyticsDto>> getUserSummary() {
        UserAnalyticsDto analytics = analyticsService.computeUserAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<ProductAnalyticsDto.ProductSummary>>> getTopProducts() {
        List<ProductAnalyticsDto.ProductSummary> products = analyticsService.getTopProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductAnalyticsDto.ProductSummary>>> getLowStockProducts() {
        List<ProductAnalyticsDto.ProductSummary> products = analyticsService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboard() {
        DashboardDto dashboard = analyticsService.computeDashboardData();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
