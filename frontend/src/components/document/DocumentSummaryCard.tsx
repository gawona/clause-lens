import type { DocumentDetail } from "../../types/document";
import {
  canRunAnalysis,
  getAnalysisButtonText,
  isAnalysisRunningStatus,
} from "../../utils/documentStatus";

interface DocumentSummaryCardProps {
  document: DocumentDetail;
  onRunAnalysis: () => void;
  isRunning: boolean;
}

const formatDate = (dateTime?: string) => {
  if (!dateTime) {
    return "-";
  }

  return dateTime.replace("T", " ").slice(0, 16);
};

const formatFileSize = (fileSize?: number) => {
  if (!fileSize) {
    return "-";
  }

  const kb = fileSize / 1024;

  if (kb < 1024) {
    return `${kb.toFixed(1)} KB`;
  }

  return `${(kb / 1024).toFixed(1)} MB`;
};

const DocumentSummaryCard = ({
  document,
  onRunAnalysis,
  isRunning,
}: DocumentSummaryCardProps) => {
  const isButtonDisabled =
    isRunning ||
    isAnalysisRunningStatus(document.analysisStatus) ||
    !canRunAnalysis(document.analysisStatus);

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-slate-500">
            {document.documentType}
          </p>

          <h2 className="mt-2 text-2xl font-bold text-slate-900">
            {document.documentName}
          </h2>

          <p className="mt-2 text-sm text-slate-500">
            {document.originalFilename}
          </p>
        </div>

        <div className="flex shrink-0 items-center gap-3">
          <span className="rounded-full bg-blue-50 px-3 py-1 text-sm font-semibold text-blue-700 ring-1 ring-blue-200">
            {document.analysisStatus}
          </span>

          <button
            type="button"
            onClick={onRunAnalysis}
            disabled={isButtonDisabled}
            className="rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300 disabled:text-slate-500"
          >
            {getAnalysisButtonText(document.analysisStatus, isRunning)}
          </button>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-3 text-sm md:grid-cols-4">
        <div className="rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium text-slate-400">문서 ID</p>
          <p className="mt-1 truncate font-medium text-slate-700">
            {document.documentId}
          </p>
        </div>

        <div className="rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium text-slate-400">업로드 상태</p>
          <p className="mt-1 font-semibold text-slate-800">
            {document.uploadStatus}
          </p>
        </div>

        <div className="rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium text-slate-400">파일 크기</p>
          <p className="mt-1 font-semibold text-slate-800">
            {formatFileSize(document.fileSize)}
          </p>
        </div>

        <div className="rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium text-slate-400">생성일</p>
          <p className="mt-1 font-semibold text-slate-800">
            {formatDate(document.createdAt)}
          </p>
        </div>
      </div>
    </section>
  );
};

export default DocumentSummaryCard;