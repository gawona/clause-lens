import { useState } from "react";
import type {
  ContractExtraction,
  ExtractionEvidence,
} from "../../types/document";

interface ContractExtractionCardProps {
  extraction: ContractExtraction | null;
}

const fieldLabels: Record<keyof Omit<ContractExtraction, "evidence">, string> =
  {
    contractTitle: "계약서 제목",
    parties: "계약 당사자",
    contractAmount: "계약 금액",
    contractPeriod: "계약 기간",
    deliveryDeadline: "납품 기한",
    paymentTerms: "지급 조건",
    penaltyClause: "지체상금 조항",
    terminationClause: "해지 조항",
  };

const formatValue = (value: unknown) => {
  if (value === null || value === undefined || value === "") {
    return "-";
  }

  if (Array.isArray(value)) {
    return value.length > 0 ? value.join(", ") : "-";
  }

  return String(value);
};

const getEvidenceKey = (evidence: ExtractionEvidence) => {
  return `${evidence.pageNumber}-${evidence.sectionTitle ?? ""}-${evidence.text}`;
};

const removeDuplicateEvidence = (evidenceList: ExtractionEvidence[]) => {
  const seen = new Set<string>();

  return evidenceList.filter((evidence) => {
    const key = getEvidenceKey(evidence);

    if (seen.has(key)) {
      return false;
    }

    seen.add(key);
    return true;
  });
};

const sortEvidenceForDisplay = (evidenceList: ExtractionEvidence[]) => {
  const deduplicated = removeDuplicateEvidence(evidenceList);

  const firstPageEvidence = deduplicated.find(
    (evidence) => evidence.pageNumber === 1
  );

  const scoredEvidence = deduplicated
    .filter((evidence) => evidence.chunkId !== firstPageEvidence?.chunkId)
    .sort((a, b) => b.score - a.score);

  if (!firstPageEvidence) {
    return scoredEvidence;
  }

  return [firstPageEvidence, ...scoredEvidence];
};

const getEvidenceBadgeText = (evidence: ExtractionEvidence) => {
  if (evidence.score === 0) {
    return "기본 포함";
  }

  return `score ${evidence.score.toFixed(2)}`;
};

const EvidenceCard = ({ evidence }: { evidence: ExtractionEvidence }) => {
  const [isTextExpanded, setIsTextExpanded] = useState(false);

  const shouldShowToggle = evidence.text.length > 220;

  return (
    <div className="rounded-xl bg-slate-50 p-4 text-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="font-semibold text-slate-700">
            {evidence.pageNumber}p
            {evidence.sectionTitle ? ` · ${evidence.sectionTitle}` : ""}
          </p>

          {evidence.score === 0 && (
            <p className="mt-1 text-xs text-slate-400">
              검색 점수 없이 핵심 문서 위치 기준으로 포함된 근거입니다.
            </p>
          )}
        </div>

        <span
          className={
            evidence.score === 0
              ? "shrink-0 rounded-full bg-slate-200 px-2 py-1 text-xs font-medium text-slate-500"
              : "shrink-0 rounded-full bg-blue-50 px-2 py-1 text-xs font-medium text-blue-600"
          }
        >
          {getEvidenceBadgeText(evidence)}
        </span>
      </div>

      <p
        className={`mt-3 whitespace-pre-line leading-6 text-slate-600 ${
          isTextExpanded ? "" : "line-clamp-4"
        }`}
      >
        {evidence.text}
      </p>

      {shouldShowToggle && (
        <button
          type="button"
          onClick={() => setIsTextExpanded((prev) => !prev)}
          className="mt-2 text-xs font-semibold text-slate-500 hover:text-slate-800"
        >
          {isTextExpanded ? "본문 접기 ▲" : "본문 더 보기 ▼"}
        </button>
      )}
    </div>
  );
};

const ContractExtractionCard = ({
  extraction,
}: ContractExtractionCardProps) => {
  const [isEvidenceExpanded, setIsEvidenceExpanded] = useState(false);

  if (!extraction) {
    return (
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-bold text-slate-900">
          핵심 항목 추출 결과
        </h3>

        <p className="mt-4 text-sm text-slate-500">
          저장된 핵심 항목 추출 결과가 없습니다.
        </p>
      </section>
    );
  }

  const fieldEntries = Object.entries(fieldLabels).map(([key, label]) => ({
    key,
    label,
    value: extraction[key as keyof Omit<ContractExtraction, "evidence">],
  }));

  const evidence = sortEvidenceForDisplay(extraction.evidence ?? []);
  const visibleEvidence = isEvidenceExpanded ? evidence : evidence.slice(0, 3);
  const hiddenEvidenceCount = Math.max(evidence.length - 3, 0);

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <h3 className="text-lg font-bold text-slate-900">핵심 항목 추출 결과</h3>

      <div className="mt-4 overflow-hidden rounded-xl border border-slate-200">
        <table className="w-full text-sm">
          <tbody className="divide-y divide-slate-200">
            {fieldEntries.map((field) => (
              <tr key={field.key}>
                <th className="w-40 bg-slate-50 px-4 py-3 text-left font-semibold text-slate-700">
                  {field.label}
                </th>

                <td className="px-4 py-3 leading-6 text-slate-800">
                  {formatValue(field.value)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-6">
        <div className="flex items-center justify-between">
          <div>
            <h4 className="text-sm font-bold text-slate-900">근거</h4>
            <p className="mt-1 text-xs text-slate-500">
              1페이지 핵심 정보와 관련도 높은 검색 근거를 우선 표시합니다.
            </p>
          </div>

          {evidence.length > 0 && (
            <span className="text-xs text-slate-400">
              {isEvidenceExpanded
                ? `전체 ${evidence.length}개 표시`
                : `대표 ${Math.min(evidence.length, 3)}개 표시`}
            </span>
          )}
        </div>

        {evidence.length === 0 ? (
          <p className="mt-3 text-sm text-slate-500">
            표시할 근거가 없습니다.
          </p>
        ) : (
          <>
            <div className="mt-3 space-y-3">
              {visibleEvidence.map((item) => (
                <EvidenceCard key={item.chunkId} evidence={item} />
              ))}
            </div>

            {hiddenEvidenceCount > 0 && (
              <button
                type="button"
                onClick={() => setIsEvidenceExpanded((prev) => !prev)}
                className="mt-4 flex w-full items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 hover:bg-slate-50"
              >
                {isEvidenceExpanded
                  ? "근거 접기 ▲"
                  : `나머지 근거 ${hiddenEvidenceCount}개 더 보기 ▼`}
              </button>
            )}
          </>
        )}
      </div>
    </section>
  );
};

export default ContractExtractionCard;