package com.revcart.analyticsservice.repository;

import com.revcart.analyticsservice.domain.entity.AnalyticsMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyticsMetricRepository extends MongoRepository<AnalyticsMetric, String> {
    
    Optional<AnalyticsMetric> findByMetricNameAndPeriod(String metricName, AnalyticsMetric.Period period);
}
