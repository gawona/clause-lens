package com.clauselens.backend.extraction.controller;

import com.clauselens.backend.extraction.dto.ContractExtractionResponse;
import com.clauselens.backend.extraction.service.DocumentExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Document Extraction",
        description = "문서 chunk와 LLM을 기반으로 핵심 항목을 JSON 형태로 추출하는 API"
)
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentExtractionController {

    private final DocumentExtractionService documentExtractionService;

    @Operation(
            summary = "계약서 핵심 항목 JSON 추출",
            description = """
                    지정한 문서의 chunk를 검색한 뒤 LLM을 사용하여 계약서 핵심 항목을 JSON 형태로 추출합니다.

                    추출 항목:
                    1. 계약명
                    2. 계약 당사자
                    3. 계약금액
                    4. 계약기간
                    5. 지급조건
                    6. 지체상금/위약금 조항
                    7. 해지조건

                    문서에서 명확히 찾을 수 없는 값은 null로 반환합니다.
                    """
    )
    @PostMapping("/{documentId}/extractions/contract")
    public ContractExtractionResponse extractContractFields(
            @Parameter(
                    description = "핵심 항목을 추출할 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        return documentExtractionService.extractContractFields(documentId);
    }

    @Operation(
            summary = "저장된 계약서 핵심 항목 추출 결과 조회",
            description = """
                지정한 문서의 저장된 계약서 핵심 항목 추출 결과를 조회합니다.

                이 API는 LLM을 다시 호출하지 않고,
                DB에 저장된 추출 결과를 반환합니다.
                """
    )
    @GetMapping("/{documentId}/extractions/contract")
    public ContractExtractionResponse getContractExtractionResult(
            @Parameter(
                    description = "핵심 항목 추출 결과를 조회할 문서 ID",
                    example = "6b630681-8409-4def-8e4f-2af22fd5ff9b",
                    required = true
            )
            @PathVariable UUID documentId
    ) {
        return documentExtractionService.getContractExtractionResult(documentId);
    }
}