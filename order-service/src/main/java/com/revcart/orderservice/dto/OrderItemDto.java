package com.revcart.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("unitPrice")
    public Double getUnitPrice() {
        return price;
    }
    
    public void setUnitPrice(Double unitPrice) {
        this.price = unitPrice;
    }
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("productImageUrl")
    public String getProductImageUrl() {
        return imageUrl;
    }
    
    public void setProductImageUrl(String productImageUrl) {
        this.imageUrl = productImageUrl;
    }
    
    @JsonProperty("subtotal")
    public Double getSubtotal() {
        return price != null && quantity != null ? price * quantity : 0.0;
    }
}
