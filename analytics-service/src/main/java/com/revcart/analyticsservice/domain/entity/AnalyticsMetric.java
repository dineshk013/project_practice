package com.revcart.analyticsservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "analytics_metrics")
public class AnalyticsMetric {
    
    @Id
    private String id;
    private String metricName;
    private Double metricValue;
    private Period period;
    private LocalDateTime recordedAt;
    private Map<String, Object> metadata;

    public enum Period {
        REALTIME, DAILY, WEEKLY, MONTHLY, YEARLY
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
        this.recordedAt = LocalDateTime.now();
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
        this.recordedAt = LocalDateTime.now();
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Map<String, Object> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }
}
