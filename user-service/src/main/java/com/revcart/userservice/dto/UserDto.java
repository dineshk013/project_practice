package com.revcart.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.revcart.userservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private User.Role role;
    private Boolean active;
    private LocalDateTime createdAt;
    
    public UserDto(Long id, String email, String name, String phone, User.Role role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    @JsonProperty("role")
    public String getRoleAsString() {
        return role != null ? role.name() : null;
    }
}
