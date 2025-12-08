package com.revcart.cartservice.service;

import com.revcart.cartservice.client.ProductServiceClient;
import com.revcart.cartservice.dto.*;
import com.revcart.cartservice.entity.Cart;
import com.revcart.cartservice.entity.CartItem;
import com.revcart.cartservice.exception.BadRequestException;
import com.revcart.cartservice.exception.ResourceNotFoundException;
import com.revcart.cartservice.repository.CartItemRepository;
import com.revcart.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    public CartDto getCart(Long userId) {
        log.info("CartService.getCart - userId: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        log.info("Cart found/created - cartId: {}, userId: {}", cart.getId(), cart.getUserId());
        if (cart.getId() != null) {
            cart.setItems(new ArrayList<>(cartItemRepository.findByCartId(cart.getId())));
            log.info("Loaded {} items from database", cart.getItems().size());
        }
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(Long userId, AddToCartRequest request) {
        log.info("CartService.addItem - userId: {}, productId: {}, quantity: {}", userId, request.getProductId(), request.getQuantity());
        // Validate product
        ApiResponse<ProductDto> productResponse = productServiceClient.getProductById(request.getProductId());
        if (!productResponse.isSuccess() || productResponse.getData() == null) {
            throw new ResourceNotFoundException("Product not found");
        }
        
        ProductDto product = productResponse.getData();
        if (!product.getActive()) {
            throw new BadRequestException("Product is not available");
        }
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        log.info("Cart retrieved/created - cartId: {}", cart.getId());

        // Check if item already exists
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            log.info("Updated existing cart item for user: {}, product: {}", userId, request.getProductId());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());
            newItem.setImageUrl(product.getImageUrl());
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
            log.info("Added new cart item for user: {}, product: {}", userId, request.getProductId());
        }

        cart.setUpdatedAt(java.time.LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return toDto(saved);
    }

    @Transactional
    public CartDto updateItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to this cart");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        log.info("Cart item updated: {}", itemId);
        return toDto(cart);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to this cart");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        log.info("Cart item removed: {}", itemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
            log.info("Cart cleared for user: {}", userId);
        });
    }

    public Integer getCartCount(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cart.getItems().stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum())
                .orElse(0);
    }

    public boolean validateCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        
        if (cart == null || cart.getId() == null) {
            log.warn("Cart not found for user: {}", userId);
            return false;
        }

        cart.setItems(new ArrayList<>(cartItemRepository.findByCartId(cart.getId())));
        
        if (cart.getItems().isEmpty()) {
            log.warn("Cart is empty for user: {}", userId);
            return false;
        }

        // Validate each item
        for (CartItem item : cart.getItems()) {
            try {
                ApiResponse<ProductDto> response = productServiceClient.getProductById(item.getProductId());
                if (!response.isSuccess() || response.getData() == null) {
                    log.warn("Product not found: {}", item.getProductId());
                    return false;
                }
                ProductDto product = response.getData();
                if (!product.getActive() || product.getStockQuantity() < item.getQuantity()) {
                    log.warn("Product {} not available or insufficient stock", item.getProductId());
                    return false;
                }
            } catch (Exception e) {
                log.error("Error validating product {}: {}", item.getProductId(), e.getMessage());
                return false;
            }
        }
        return true;
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    private CartDto toDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setItems(cart.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList()));
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setTotalItems(cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());
        return dto;
    }

    private CartItemDto toItemDto(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getImageUrl()
        );
    }
}
