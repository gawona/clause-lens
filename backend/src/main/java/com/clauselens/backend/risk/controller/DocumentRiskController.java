package com.clauselens.backend.risk.controller;

import com.clauselens.backend.risk.dto.RiskDetectionResponse;
import com.clauselens.backend.risk.service.DocumentRiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document Risk",
        description = "문서 chunk와 LLM을 기반으로 위험 조항과 누락 항목을 탐지하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentRiskController {

    private final DocumentRiskService documentRiskService;

    @Operation(
            summary = "문서 위험 조항 탐지",
            description = """
                    지정한 문서의 chunk를 검색한 뒤 LLM을 사용하여 위험 조항과 누락 가능성이 있는 항목을 탐지합니다.

                    탐지 항목 예시:
                    1. 계약금액 누락
                    2. 계약기간 또는 납품기한 불명확
                    3. 검수 기준 불명확
                    4. 지체상금 또는 위약금 과도
                    5. 해지 조건 불명확
                    6. 손해배상 범위 과도
                    7. 개인정보 처리 또는 보안 조항 미흡
                    8. 저작권·지식재산권 귀속 불명확

                    응답에는 위험 수준, 위험 유형, 판단 이유, 근거 문장, 보완 권고를 포함합니다.
                    """
    )
    @PostMapping("/{documentId}/risks")
    public RiskDetectionResponse detectRisks(
            @Parameter(
                    description = "위험 조항을 탐지할 문서 ID",
                    example = "9189bf81-e589-4807-bbf1-e76a0ff0d363",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        return documentRiskService.detectRisks(documentId);
    }
    @Operation(
            summary = "저장된 문서 위험 조항 탐지 결과 조회",
            description = """
                지정한 문서의 저장된 위험 조항 탐지 결과를 조회합니다.

                이 API는 LLM을 다시 호출하지 않고,
                DB에 저장된 위험 조항 탐지 결과를 반환합니다.
                """
    )
    @GetMapping("/{documentId}/risks")
    public RiskDetectionResponse getRiskResult(
            @Parameter(
                    description = "위험 조항 탐지 결과를 조회할 문서 ID",
                    example = "9189bf81-e589-4807-bbf1-e76a0ff0d363",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        return documentRiskService.getRiskResult(documentId);
    }
}

