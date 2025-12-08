package com.revcart.productservice.controller;

import com.revcart.productservice.dto.*;
import com.revcart.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProductsForAnalytics() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved successfully"));
    }

    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByCategory(@PathVariable String slug) {
        List<ProductDto> products = productService.getProductsByCategorySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(@RequestParam String keyword) {
        List<ProductDto> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success(products, "Search completed successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@RequestBody ProductDto productDto) {
        ProductDto created = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Product created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto) {
        ProductDto updated = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<StockResponse>> checkStock(@PathVariable Long id) {
        StockResponse stock = productService.checkStock(id);
        return ResponseEntity.ok(ApiResponse.success(stock, "Stock checked successfully"));
    }

    @PutMapping("/stock/reserve")
    public ResponseEntity<ApiResponse<Void>> reserveStock(@RequestBody StockReservationRequest request) {
        productService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock reserved successfully"));
    }

    @PutMapping("/stock/release")
    public ResponseEntity<ApiResponse<Void>> releaseStock(@RequestBody StockReservationRequest request) {
        productService.releaseStock(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock released successfully"));
    }
    
    @GetMapping("/admin/products/stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getProductStats() {
        java.util.Map<String, Object> stats = productService.getProductStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Product stats retrieved successfully"));
    }
}
