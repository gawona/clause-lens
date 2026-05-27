package com.clauselens.backend.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "opensearch")
public class OpenSearchProperties {

    private String url;
    private Index index = new Index();

    @Getter
    @Setter
    public static class Index {
        private String documentChunks;
    }
}