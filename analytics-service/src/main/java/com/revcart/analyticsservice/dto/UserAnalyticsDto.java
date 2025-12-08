package com.revcart.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsDto {
    private Long totalUsers;
    private Long newUsersThisMonth;
    private Long returningCustomers;
}
