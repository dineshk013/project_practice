package com.revcart.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private OrderAnalyticsDto orderAnalytics;
    private ProductAnalyticsDto productAnalytics;
    private UserAnalyticsDto userAnalytics;
}
