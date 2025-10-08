package com.cp.lab09sec1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.cp.lab09sec1.StaffApiProperties;

@Configuration
public class ApiClientConfig {

    @Bean
    public WebClient staffApiWebClient(WebClient.Builder builder, StaffApiProperties properties) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
