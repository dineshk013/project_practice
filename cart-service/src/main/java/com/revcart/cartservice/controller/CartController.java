package com.revcart.cartservice.controller;

import com.revcart.cartservice.dto.AddToCartRequest;
import com.revcart.cartservice.dto.ApiResponse;
import com.revcart.cartservice.dto.CartDto;
import com.revcart.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("GET /api/cart - X-User-Id: {}", userId);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("X-User-Id header is required"));
        }
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/cart/items - X-User-Id: {}, productId: {}", userId, request.getProductId());
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("X-User-Id header is required"));
        }
        CartDto cart = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Item added to cart"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        CartDto cart = cartService.updateItem(userId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from cart"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("X-User-Id header is required"));
        }
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartCount(@RequestHeader("X-User-Id") Long userId) {
        Integer count = cartService.getCartCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Cart count retrieved"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("X-User-Id header is required"));
        }
        boolean valid = cartService.validateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(valid, "Cart validation completed"));
    }
}
