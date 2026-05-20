package com.clauselens.backend.analysis.client;

import com.clauselens.backend.analysis.dto.ExtractRequest;
import com.clauselens.backend.analysis.dto.ExtractResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AnalysisEngineClient {

    private final RestClient restClient;

    @Value("${analysis-engine.base-url}")
    private String analysisEngineBaseUrl;

    public ExtractResponse extractText(ExtractRequest request) {
        return restClient.post()
                .uri(analysisEngineBaseUrl + "/api/pdf/extract")
                .body(request)
                .retrieve()
                .body(ExtractResponse.class);
    }
}