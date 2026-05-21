package com.clauselens.backend.search.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
public class OpenSearchConfig {

    @Bean(name = "openSearchRestClient")
    public RestClient openSearchRestClient(OpenSearchProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getUrl())
                .build();
    }
}