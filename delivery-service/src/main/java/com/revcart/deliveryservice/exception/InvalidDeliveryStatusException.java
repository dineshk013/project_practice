package com.revcart.deliveryservice.exception;

public class InvalidDeliveryStatusException extends RuntimeException {
    public InvalidDeliveryStatusException(String message) {
        super(message);
    }
}
