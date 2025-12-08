package com.revcart.analyticsservice.exception;

public class AnalyticsComputationException extends RuntimeException {
    
    public AnalyticsComputationException(String message) {
        super(message);
    }

    public AnalyticsComputationException(String message, Throwable cause) {
        super(message, cause);
    }
}
