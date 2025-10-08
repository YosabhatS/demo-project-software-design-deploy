package com.cp.lab09sec1.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	@Bean
	public WebClient menuItemWebClient(WebClient.Builder builder) {  
		return builder.baseUrl("http://localhost:8085").build();
	}
	@Bean
    public DataServiceClient dataServiceClient(WebClient menuItemWebClient) {
        return new DataServiceClient(menuItemWebClient);
    }

}
