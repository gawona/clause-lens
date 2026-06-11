import type { AnalysisStatus } from "../types/document";

export const isAnalysisRunningStatus = (status?: AnalysisStatus) => {
  return (
    status === "ANALYZING" ||
    status === "EXTRACTING" ||
    status === "CHUNKING" ||
    status === "INDEXING"
  );
};

export const canRunAnalysis = (status?: AnalysisStatus) => {
  return status === "UPLOADED" || status === "FAILED";
};

export const getAnalysisButtonText = (
  status?: AnalysisStatus,
  isRunning?: boolean
) => {
  if (isRunning || isAnalysisRunningStatus(status)) {
    return "분석 실행 중...";
  }

  if (status === "COMPLETED") {
    return "분석 완료";
  }

  if (status === "FAILED") {
    return "다시 분석 실행";
  }

  return "전체 분석 실행";
};