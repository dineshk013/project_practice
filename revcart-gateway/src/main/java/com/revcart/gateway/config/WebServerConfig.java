package com.revcart.gateway.config;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.reactive.server.ConfigurableReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableReactiveWebServerFactory> webServerFactoryCustomizer() {
        return factory -> factory.setPort(8080);
    }
}
