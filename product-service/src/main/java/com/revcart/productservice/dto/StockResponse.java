package com.revcart.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long productId;
    private Integer availableStock;
    private Boolean inStock;
}
