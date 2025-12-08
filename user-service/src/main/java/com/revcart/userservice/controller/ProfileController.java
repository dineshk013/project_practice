package com.revcart.userservice.controller;

import com.revcart.userservice.dto.AddressDto;
import com.revcart.userservice.dto.ApiResponse;
import com.revcart.userservice.dto.UserDto;
import com.revcart.userservice.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getProfile() {
        UserDto user = profileService.getProfile();
        return ResponseEntity.ok(ApiResponse.success(user, "Profile retrieved successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody UserDto userDto) {
        UserDto updated = profileService.updateProfile(userDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddresses() {
        List<AddressDto> addresses = profileService.getAddresses();
        return ResponseEntity.ok(ApiResponse.success(addresses, "Addresses retrieved successfully"));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(@RequestBody AddressDto addressDto) {
        AddressDto created = profileService.addAddress(addressDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Address added successfully"));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressDto addressDto) {
        AddressDto updated = profileService.updateAddress(id, addressDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Address updated successfully"));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        profileService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }
}
