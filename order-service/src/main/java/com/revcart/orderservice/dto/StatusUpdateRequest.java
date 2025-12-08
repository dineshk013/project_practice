package com.revcart.orderservice.dto;

import lombok.Data;

@Data
public class StatusUpdateRequest {

    // Different possible field names that the frontend might send
    private String status;
    private String newStatus;
    private String orderStatus;

    /**
     * Returns the first non-blank status field.
     */
    public String resolveStatus() {
        if (status != null && !status.isBlank()) {
            return status;
        }
        if (newStatus != null && !newStatus.isBlank()) {
            return newStatus;
        }
        if (orderStatus != null && !orderStatus.isBlank()) {
            return orderStatus;
        }
        return null;
    }
}
