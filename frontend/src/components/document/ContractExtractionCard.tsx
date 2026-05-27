import type { ContractExtraction } from "../../types/document";

interface ContractExtractionCardProps {
  extraction: ContractExtraction | null;
}

const labelMap: Record<string, string> = {
  contractTitle: "계약서 제목",
  parties: "계약 당사자",
  contractAmount: "계약 금액",
  startDate: "시작일",
  endDate: "종료일",
  paymentTerms: "지급 조건",
  penaltyClause: "지체상금 조항",
  terminationClause: "해지 조항",
};

const ContractExtractionCard = ({
  extraction,
}: ContractExtractionCardProps) => {
  const entries = extraction ? Object.entries(extraction) : [];

  return (
    <section className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
      <h3 className="text-lg font-bold text-gray-900">핵심 항목 추출 결과</h3>

      {!extraction || entries.length === 0 ? (
        <p className="mt-4 text-sm text-gray-500">
          저장된 핵심 항목 추출 결과가 없습니다.
        </p>
      ) : (
        <div className="mt-4 overflow-hidden rounded-lg border border-gray-200">
          <table className="w-full text-sm">
            <tbody className="divide-y divide-gray-200">
              {entries.map(([key, value]) => (
                <tr key={key}>
                  <th className="w-40 bg-gray-50 px-4 py-3 text-left font-medium text-gray-700">
                    {labelMap[key] || key}
                  </th>
                  <td className="px-4 py-3 text-gray-800">
                    {value === null || value === undefined || value === ""
                      ? "-"
                      : String(value)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

export default ContractExtractionCard;