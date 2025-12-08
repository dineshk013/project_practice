package com.revcart.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalyticsDto {
    private Long totalOrders;
    private Double totalRevenue;
    private Double avgOrderValue;
    private Long ordersToday;
    private Long ordersThisWeek;
    private Long ordersThisMonth;
}
