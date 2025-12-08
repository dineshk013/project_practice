package com.revcart.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalyticsDto {
    private List<ProductSummary> topProducts;
    private List<ProductSummary> lowStockProducts;
    private Long totalProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private Long id;
        private String name;
        private Integer stock;
        private Double price;
    }
}
