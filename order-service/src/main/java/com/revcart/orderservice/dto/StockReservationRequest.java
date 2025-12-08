package com.revcart.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationRequest {
    private String reservationId;
    private List<StockItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem {
        private Long productId;
        private Integer quantity;
    }
}
