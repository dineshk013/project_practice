package com.revcart.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long id;
    private String street;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String zipCode;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private Boolean primaryAddress;
}
