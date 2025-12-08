package com.revcart.orderservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    String authorization = attributes.getRequest().getHeader("Authorization");
                    if (authorization != null) {
                        template.header("Authorization", authorization);
                    }
                    
                    String userId = attributes.getRequest().getHeader("X-User-Id");
                    if (userId != null) {
                        template.header("X-User-Id", userId);
                        org.slf4j.LoggerFactory.getLogger(FeignConfig.class)
                            .info("🔑 Feign request to {} with X-User-Id: {}", template.url(), userId);
                    }
                }
            }
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
