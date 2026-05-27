import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getDocuments } from "../api/documentApi";
import type { DocumentItem } from "../types/document";

const getStatusClassName = (status?: string) => {
  switch (status) {
    case "COMPLETED":
      return "bg-emerald-50 text-emerald-700 ring-emerald-200";
    case "FAILED":
      return "bg-red-50 text-red-700 ring-red-200";
    case "ANALYZING":
    case "EXTRACTING":
    case "CHUNKING":
    case "INDEXING":
      return "bg-blue-50 text-blue-700 ring-blue-200";
    default:
      return "bg-slate-50 text-slate-600 ring-slate-200";
  }
};

const formatDate = (dateTime: string) => {
  return dateTime.replace("T", " ").slice(0, 16);
};

const formatFileSize = (fileSize: number) => {
  const kb = fileSize / 1024;

  if (kb < 1024) {
    return `${kb.toFixed(1)} KB`;
  }

  return `${(kb / 1024).toFixed(1)} MB`;
};

const DocumentListPage = () => {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const fetchDocuments = async () => {
      try {
        setIsLoading(true);
        const result = await getDocuments();

        console.log("문서 목록:", result);

        setDocuments(result);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDocuments();
  }, []);

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <section className="mb-8 rounded-3xl bg-slate-900 px-8 py-10 text-white shadow-sm">
        <p className="text-sm font-medium text-slate-300">ClauseLens</p>

        <h1 className="mt-3 text-3xl font-bold tracking-tight">
          계약서·약관·정책 문서를 근거 기반으로 분석합니다.
        </h1>

        <p className="mt-4 max-w-2xl text-sm leading-6 text-slate-300">
          PDF 문서를 업로드하고, 핵심 항목 추출 결과와 위험 조항 탐지 결과,
          문서 기반 질의응답을 확인할 수 있습니다.
        </p>
      </section>

      <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2 className="text-lg font-bold text-slate-900">문서 목록</h2>
            <p className="mt-1 text-sm text-slate-500">
              업로드된 문서를 선택해 분석 결과를 확인합니다.
            </p>
          </div>

          <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
            총 {documents.length}개
          </span>
        </div>

        {isLoading ? (
          <div className="p-8 text-sm text-slate-500">
            문서 목록을 불러오는 중입니다.
          </div>
        ) : documents.length === 0 ? (
          <div className="flex flex-col items-center justify-center px-6 py-20 text-center">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-2xl">
              📄
            </div>

            <h3 className="mt-4 text-base font-semibold text-slate-900">
              아직 업로드된 문서가 없습니다.
            </h3>

            <p className="mt-2 text-sm text-slate-500">
              PDF 업로드 화면을 연결하면 이곳에 문서가 표시됩니다.
            </p>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {documents.map((document) => (
              <Link
                key={document.documentId}
                to={`/documents/${document.documentId}`}
                className="group flex items-center justify-between gap-4 px-6 py-5 no-underline transition hover:bg-slate-50"
              >
                <div className="min-w-0">
                  <h3 className="truncate font-semibold text-slate-900 group-hover:text-blue-700">
                    {document.documentName}
                  </h3>

                  <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-slate-500">
                    <span>{document.originalFilename}</span>
                    <span>·</span>
                    <span>{document.documentType}</span>
                    <span>·</span>
                    <span>{formatFileSize(document.fileSize)}</span>
                    <span>·</span>
                    <span>{formatDate(document.createdAt)}</span>
                  </div>
                </div>

                <div className="flex shrink-0 items-center gap-3">
                  <span
                    className={`rounded-full px-3 py-1 text-xs font-semibold ring-1 ${getStatusClassName(
                      document.analysisStatus
                    )}`}
                  >
                    {document.analysisStatus}
                  </span>

                  <span className="text-sm font-medium text-slate-400 group-hover:text-blue-600">
                    상세보기
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  );
};

export default DocumentListPage;