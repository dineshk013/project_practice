package com.revcart.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto implements Serializable {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private String imageUrl;
}
