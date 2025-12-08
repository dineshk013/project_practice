package com.revcart.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String sku;
    private String brand;
    private String highlights;
    private Boolean active;
    private CategoryDto category;
    private Integer stockQuantity;
    private Long categoryId;
    private String categoryName;
    private Integer availableQuantity;
}
