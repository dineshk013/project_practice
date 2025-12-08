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
    private LocalDateTime createdAt;
    
    @JsonProperty("role")
    public String getRoleAsString() {
        return role != null ? role.name() : null;
    }
}
