import type { RiskItem, RiskLevel } from "../../types/document";

interface RiskClauseListProps {
  risks: RiskItem[];
}

const riskPriority: Record<RiskLevel, number> = {
  HIGH: 0,
  MEDIUM: 1,
  LOW: 2,
};

const getRiskClassName = (riskLevel?: RiskLevel) => {
  switch (riskLevel) {
    case "HIGH":
      return "border-red-200 bg-red-50 text-red-700";
    case "MEDIUM":
      return "border-yellow-200 bg-yellow-50 text-yellow-700";
    case "LOW":
      return "border-green-200 bg-green-50 text-green-700";
    default:
      return "border-slate-200 bg-slate-50 text-slate-700";
  }
};

const RiskClauseList = ({ risks }: RiskClauseListProps) => {
  const sortedRisks = [...risks].sort((a, b) => {
    const aPriority = a.riskLevel ? riskPriority[a.riskLevel] : 99;
    const bPriority = b.riskLevel ? riskPriority[b.riskLevel] : 99;

    return aPriority - bPriority;
  });

  const highCount = risks.filter((risk) => risk.riskLevel === "HIGH").length;
  const mediumCount = risks.filter((risk) => risk.riskLevel === "MEDIUM").length;
  const lowCount = risks.filter((risk) => risk.riskLevel === "LOW").length;

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h3 className="text-lg font-bold text-slate-900">
            위험 조항 탐지 결과
          </h3>
          <p className="mt-1 text-sm text-slate-500">
            위험 수준이 높은 항목부터 표시합니다.
          </p>
        </div>

        <div className="flex shrink-0 gap-2 text-xs font-semibold">
          <span className="rounded-full bg-red-50 px-2 py-1 text-red-700">
            HIGH {highCount}
          </span>
          <span className="rounded-full bg-yellow-50 px-2 py-1 text-yellow-700">
            MEDIUM {mediumCount}
          </span>
          <span className="rounded-full bg-green-50 px-2 py-1 text-green-700">
            LOW {lowCount}
          </span>
        </div>
      </div>

      {sortedRisks.length === 0 ? (
        <p className="mt-4 text-sm text-slate-500">
          탐지된 위험 조항이 없습니다.
        </p>
      ) : (
        <div className="mt-4 space-y-4">
          {sortedRisks.map((risk, index) => (
            <article
              key={`${risk.riskType}-${index}`}
              className="rounded-xl border border-slate-200 p-4"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h4 className="font-semibold text-slate-900">
                    {risk.riskType || "위험 유형 없음"}
                  </h4>

                  <p className="mt-2 text-sm leading-6 text-slate-700">
                    {risk.reason || "-"}
                  </p>
                </div>

                <span
                  className={`rounded-full border px-3 py-1 text-xs font-bold ${getRiskClassName(
                    risk.riskLevel
                  )}`}
                >
                  {risk.riskLevel || "UNKNOWN"}
                </span>
              </div>

              {risk.evidence && (
                <div className="mt-4 rounded-xl bg-slate-50 p-3">
                  <p className="text-xs font-medium text-slate-500">
                    근거 페이지:{" "}
                    {risk.evidence.pageNumber
                      ? `${risk.evidence.pageNumber}p`
                      : "-"}
                    {risk.evidence.sectionTitle
                      ? ` · ${risk.evidence.sectionTitle}`
                      : ""}
                  </p>

                  <p className="mt-2 whitespace-pre-line text-sm leading-6 text-slate-700">
                    {risk.evidence.text || "-"}
                  </p>
                </div>
              )}

              {risk.recommendation && (
                <p className="mt-3 text-sm leading-6 text-slate-700">
                  <span className="font-semibold text-slate-900">
                    권장사항:{" "}
                  </span>
                  {risk.recommendation}
                </p>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
};

export default RiskClauseList;