import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
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
  const [analysisErrorMessage, setAnalysisErrorMessage] = useState<
    string | null
  >(null);

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
        setExtraction(null);
      }

      if (risksResult.status === "fulfilled") {
        setRisks(risksResult.value);
      } else {
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
      setAnalysisErrorMessage(null);

      await runAnalysis(documentId);
      await fetchDocumentResult();
    } catch (error) {
      console.error("문서 전체 분석 실행 실패:", error);
      setAnalysisErrorMessage(
        "문서 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
      );
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
        <p className="text-sm text-slate-500">
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

  const isNotStarted = document.analysisStatus === "NOT_STARTED";
  const isCompleted = document.analysisStatus === "COMPLETED";

  return (
    <main className="mx-auto max-w-6xl px-6 py-8">
      <div className="mb-6">
        <Link
          to="/"
          className="text-sm font-medium text-slate-500 no-underline hover:text-slate-900"
        >
          ← 문서 목록으로 돌아가기
        </Link>

        <h1 className="mt-4 text-2xl font-bold text-slate-900">
          문서 분석 결과
        </h1>

        <p className="mt-2 text-sm text-slate-500">
          핵심 항목, 위험 조항, 근거 기반 질의응답 결과를 확인합니다.
        </p>
      </div>

      <div className="space-y-6">
        <DocumentSummaryCard
          document={document}
          onRunAnalysis={handleRunAnalysis}
          isRunning={isRunning}
        />

        {isNotStarted && !isRunning && (
          <div className="rounded-2xl border border-slate-200 bg-white px-5 py-4 text-sm text-slate-600">
            아직 분석이 실행되지 않은 문서입니다. 상단의{" "}
            <span className="font-semibold text-slate-900">
              전체 분석 실행
            </span>{" "}
            버튼을 눌러 핵심 항목 추출과 위험 조항 탐지를 시작하세요.
          </div>
        )}

        {isRunning && (
          <div className="rounded-2xl border border-blue-200 bg-blue-50 px-5 py-4 text-sm text-blue-700">
            <p className="font-semibold">문서 분석을 실행 중입니다.</p>
            <p className="mt-1">
              PDF 텍스트 추출, chunk 생성, OpenSearch 색인, 핵심 항목 추출,
              위험 조항 탐지를 순차적으로 수행합니다. 잠시만 기다려주세요.
            </p>
          </div>
        )}

        {analysisErrorMessage && (
          <div className="rounded-2xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700">
            {analysisErrorMessage}
          </div>
        )}

        <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
          <ContractExtractionCard extraction={extraction} />
          <RiskClauseList risks={risks} />
        </div>

        <QuestionBox documentId={documentId} disabled={!isCompleted} />
      </div>
    </main>
  );
};

export default DocumentDetailPage;