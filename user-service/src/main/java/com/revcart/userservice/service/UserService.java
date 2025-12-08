package com.revcart.userservice.service;

import com.revcart.userservice.dto.AddressDto;
import com.revcart.userservice.dto.UserDto;
import com.revcart.userservice.entity.Address;
import com.revcart.userservice.entity.User;
import com.revcart.userservice.exception.BadRequestException;
import com.revcart.userservice.exception.ResourceNotFoundException;
import com.revcart.userservice.repository.AddressRepository;
import com.revcart.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserDto getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserDto(user);
    }

    @Transactional
    public UserDto updateProfile(UserDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        User updated = userRepository.save(user);
        log.info("Profile updated: {}", updated.getEmail());
        return toUserDto(updated);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserDto(user);
    }

    public List<AddressDto> getAddresses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::toAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDto addAddress(AddressDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setStreet(dto.getStreet() != null ? dto.getStreet() : dto.getLine1());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode() != null ? dto.getZipCode() : dto.getPostalCode());
        address.setCountry(dto.getCountry() != null ? dto.getCountry() : "India");
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 
                            (dto.getPrimaryAddress() != null ? dto.getPrimaryAddress() : false));

        Address saved = addressRepository.save(address);
        log.info("Address added for user: {}", user.getEmail());
        return toAddressDto(saved);
    }

    @Transactional
    public AddressDto updateAddress(Long id, AddressDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Unauthorized to update this address");
        }

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        address.setIsDefault(dto.getIsDefault());

        Address updated = addressRepository.save(address);
        log.info("Address updated: {}", updated.getId());
        return toAddressDto(updated);
    }

    @Transactional
    public void deleteAddress(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Unauthorized to delete this address");
        }

        addressRepository.delete(address);
        log.info("Address deleted: {}", id);
    }

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto(user.getId(), user.getEmail(), user.getName(), user.getPhone(), user.getRole(), user.getCreatedAt());
        dto.setActive(user.getActive());
        return dto;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    private AddressDto toAddressDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setLine1(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setPostalCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setIsDefault(address.getIsDefault());
        dto.setPrimaryAddress(address.getIsDefault());
        return dto;
    }
    
    public java.util.Map<String, Object> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        long activeUsers = userRepository.countByActive(true);
        stats.put("activeUsers", activeUsers);
        return stats;
    }
    
    public org.springframework.data.domain.Page<UserDto> getAllUsersPaged(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDto);
    }
    
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
    
    public List<UserDto> getDeliveryAgents() {
        return userRepository.findByRole(User.Role.DELIVERY_AGENT).stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }
}
