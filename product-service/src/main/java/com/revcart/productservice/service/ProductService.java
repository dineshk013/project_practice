package com.revcart.productservice.service;

import com.revcart.productservice.dto.*;
import com.revcart.productservice.entity.Product;
import com.revcart.productservice.exception.InsufficientStockException;
import com.revcart.productservice.exception.ResourceNotFoundException;
import com.revcart.productservice.repository.ProductRepository;
import com.revcart.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDto(product);
    }

    public List<ProductDto> getProductsByCategorySlug(String slug) {
        return productRepository.findByCategorySlug(slug).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = toEntity(dto);
        Long catId = dto.getCategoryId() != null ? dto.getCategoryId() : 
                     (dto.getCategory() != null ? dto.getCategory().getId() : null);
        if (catId != null) {
            product.setCategory(categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
        }
        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setBrand(dto.getBrand());
        product.setHighlights(dto.getHighlights());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        
        Long catId = dto.getCategoryId() != null ? dto.getCategoryId() : 
                     (dto.getCategory() != null ? dto.getCategory().getId() : null);
        if (catId != null) {
            product.setCategory(categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
        }
        
        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());
        return toDto(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    public StockResponse checkStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return new StockResponse(product.getId(), product.getStockQuantity(), product.getStockQuantity() > 0);
    }

    @Transactional
    public void reserveStock(StockReservationRequest request) {
        for (StockReservationRequest.StockItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
            
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
            log.info("Reserved {} units of product {}", item.getQuantity(), product.getId());
        }
    }

    @Transactional
    public void releaseStock(StockReservationRequest request) {
        for (StockReservationRequest.StockItem item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
            
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
            log.info("Released {} units of product {}", item.getQuantity(), product.getId());
        }
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setSku(product.getSku());
        dto.setBrand(product.getBrand());
        dto.setHighlights(product.getHighlights());
        dto.setActive(product.getActive());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setAvailableQuantity(product.getStockQuantity());
        
        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            categoryDto.setSlug(product.getCategory().getSlug());
            categoryDto.setDescription(product.getCategory().getDescription());
            categoryDto.setImageUrl(product.getCategory().getImageUrl());
            dto.setCategory(categoryDto);
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        return dto;
    }

    private Product toEntity(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setSku(dto.getSku());
        product.setBrand(dto.getBrand());
        product.setHighlights(dto.getHighlights());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        return product;
    }
    
    public java.util.Map<String, Object> getProductStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        long totalProducts = productRepository.count();
        stats.put("totalProducts", totalProducts);
        return stats;
    }
}
