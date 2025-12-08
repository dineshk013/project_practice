package com.revcart.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto implements Serializable {
    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private Double totalPrice;
    private Integer totalItems;
}
