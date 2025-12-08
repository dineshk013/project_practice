package com.revcart.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long id;
    
    @JsonProperty("street")
    private String street;
    
    @JsonProperty("line1")
    public String getLine1() {
        return street;
    }
    
    public void setLine1(String line1) {
        this.street = line1;
    }
    
    private String city;
    private String state;
    
    @JsonProperty("zipCode")
    private String zipCode;
    
    @JsonProperty("postalCode")
    public String getPostalCode() {
        return zipCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.zipCode = postalCode;
    }
    
    private String country;
}
