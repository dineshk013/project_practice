package com.revcart.userservice.controller;

import com.revcart.userservice.dto.ApiResponse;
import com.revcart.userservice.dto.UserDto;
import com.revcart.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users fetched"));
    }

    @GetMapping("/users/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        Map<String, Object> stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "User stats retrieved successfully"));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    @GetMapping("/count/active")
    public long getActiveUsers() {
        return userService.countActiveUsers();
    }
    
    @GetMapping("/delivery-agents")
    public ResponseEntity<ApiResponse<List<UserDto>>> getDeliveryAgents() {
        List<UserDto> agents = userService.getDeliveryAgents();
        return ResponseEntity.ok(ApiResponse.success(agents, "Delivery agents retrieved successfully"));
    }
}
