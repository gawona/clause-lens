import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  getContractExtraction,
  getDocumentDetail,
  getRisks,
  runAnalysis,
} from "../api/documentApi";
import ContractExtractionCard from "../components/document/ContractExtractionCard";
import DocumentSummaryCard from "../components/document/DocumentSummaryCard";
import QuestionBox from "../components/document/QuestionBox";
import RiskClauseList from "../components/document/RiskClauseList";
import type {
  ContractExtraction,
  DocumentDetail,
  RiskItem,
} from "../types/document";

const DocumentDetailPage = () => {
  const { documentId } = useParams<{ documentId: string }>();

  const [document, setDocument] = useState<DocumentDetail | null>(null);
  const [extraction, setExtraction] = useState<ContractExtraction | null>(null);
  const [risks, setRisks] = useState<RiskItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isRunning, setIsRunning] = useState(false);

  const fetchDocumentResult = async () => {
    if (!documentId) {
      return;
    }
  
    try {
      setIsLoading(true);
  
      const [documentResult, extractionResult, risksResult] =
        await Promise.allSettled([
          getDocumentDetail(documentId),
          getContractExtraction(documentId),
          getRisks(documentId),
        ]);
  
      if (documentResult.status === "fulfilled") {
        setDocument(documentResult.value);
      } else {
        console.error("문서 상세 조회 실패:", documentResult.reason);
        setDocument(null);
      }
  
      if (extractionResult.status === "fulfilled") {
        setExtraction(extractionResult.value);
      } else {
        console.error("핵심 항목 조회 실패:", extractionResult.reason);
        setExtraction(null);
      }
  
      if (risksResult.status === "fulfilled") {
        setRisks(risksResult.value);
      } else {
        console.error("위험 조항 조회 실패:", risksResult.reason);
        setRisks([]);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleRunAnalysis = async () => {
    if (!documentId || !document) {
      return;
    }
  
    if (document.analysisStatus === "COMPLETED") {
      return;
    }
  
    try {
      setIsRunning(true);
  
      await runAnalysis(documentId);
      await fetchDocumentResult();
    } catch (error) {
      console.error("문서 전체 분석 실행 실패:", error);
      alert("문서 분석 실행 중 오류가 발생했습니다.");
    } finally {
      setIsRunning(false);
    }
  };

  useEffect(() => {
    fetchDocumentResult();
  }, [documentId]);

  if (!documentId) {
    return (
      <main className="mx-auto max-w-6xl px-6 py-8">
        <p className="text-sm text-red-600">문서 ID가 없습니다.</p>
      </main>
    );
  }

  if (isLoading) {
    return (
      <main className="mx-auto max-w-6xl px-6 py-8">
        <p className="text-sm text-gray-500">
          문서 분석 결과를 불러오는 중입니다.
        </p>
      </main>
    );
  }

  if (!document) {
    return (
      <main className="mx-auto max-w-6xl px-6 py-8">
        <p className="text-sm text-red-600">문서 정보를 찾을 수 없습니다.</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-6xl px-6 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">문서 분석 결과</h1>
        <p className="mt-2 text-sm text-gray-500">
          핵심 항목, 위험 조항, 근거 기반 질의응답 결과를 확인합니다.
        </p>
      </div>

      <div className="space-y-6">
        <DocumentSummaryCard
          document={document}
          onRunAnalysis={handleRunAnalysis}
          isRunning={isRunning}
        />
        <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
          <ContractExtractionCard extraction={extraction} />
          <RiskClauseList risks={risks} />
        </div>

        <QuestionBox documentId={documentId} />
      </div>
    </main>
  );
};

export default DocumentDetailPage;