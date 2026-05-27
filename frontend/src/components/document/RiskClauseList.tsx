import type { RiskItem, RiskLevel } from "../../types/document";

interface RiskClauseListProps {
  risks: RiskItem[];
}

const getRiskClassName = (riskLevel?: RiskLevel) => {
  switch (riskLevel) {
    case "HIGH":
      return "border-red-200 bg-red-50 text-red-700";
    case "MEDIUM":
      return "border-yellow-200 bg-yellow-50 text-yellow-700";
    case "LOW":
      return "border-green-200 bg-green-50 text-green-700";
    default:
      return "border-gray-200 bg-gray-50 text-gray-700";
  }
};

const RiskClauseList = ({ risks }: RiskClauseListProps) => {
  return (
    <section className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
      <h3 className="text-lg font-bold text-gray-900">위험 조항 탐지 결과</h3>

      {risks.length === 0 ? (
        <p className="mt-4 text-sm text-gray-500">
          탐지된 위험 조항이 없습니다.
        </p>
      ) : (
        <div className="mt-4 space-y-4">
          {risks.map((risk, index) => (
            <article
              key={`${risk.riskType}-${index}`}
              className="rounded-lg border border-gray-200 p-4"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h4 className="font-semibold text-gray-900">
                    {risk.riskType || "위험 유형 없음"}
                  </h4>

                  <p className="mt-2 text-sm leading-6 text-gray-700">
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
                <div className="mt-4 rounded-lg bg-gray-50 p-3">
                  <p className="text-xs font-medium text-gray-500">
                    근거 페이지: {risk.evidence.page || "-"}
                    {risk.evidence.section
                      ? ` · ${risk.evidence.section}`
                      : ""}
                  </p>

                  <p className="mt-2 text-sm leading-6 text-gray-700">
                    {risk.evidence.text || "-"}
                  </p>
                </div>
              )}

              {risk.recommendation && (
                <p className="mt-3 text-sm leading-6 text-gray-700">
                  <span className="font-semibold text-gray-900">권장사항: </span>
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