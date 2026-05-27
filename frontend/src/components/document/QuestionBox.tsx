import { useState } from "react";
import { askQuestion } from "../../api/documentApi";
import type { QuestionResponse } from "../../types/document";

interface QuestionBoxProps {
  documentId: string;
}

const QuestionBox = ({ documentId }: QuestionBoxProps) => {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState<QuestionResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleAskQuestion = async () => {
    if (!question.trim()) {
      return;
    }

    try {
      setIsLoading(true);
      const result = await askQuestion(documentId, question.trim());
      setAnswer(result);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <section className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
      <h3 className="text-lg font-bold text-gray-900">문서 기반 질의응답</h3>

      <div className="mt-4 flex gap-2">
        <input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              handleAskQuestion();
            }
          }}
          placeholder="예: 이 계약서에서 위약금 조항 찾아줘"
          className="flex-1 rounded-lg border border-gray-300 px-4 py-2 text-sm outline-none focus:border-blue-500"
        />

        <button
          type="button"
          onClick={handleAskQuestion}
          disabled={isLoading}
          className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:bg-gray-300"
        >
          {isLoading ? "질문 중..." : "질문"}
        </button>
      </div>

      {answer && (
        <div className="mt-5 space-y-4">
          <div>
            <p className="text-sm font-semibold text-gray-900">답변</p>
            <p className="mt-2 text-sm leading-6 text-gray-700">
              {answer.answer}
            </p>
          </div>

          {answer.confidence !== undefined && answer.confidence !== null && (
            <p className="text-sm text-gray-500">
              confidence: {answer.confidence}
            </p>
          )}

          <div>
            <p className="text-sm font-semibold text-gray-900">근거</p>

            {!answer.evidence || answer.evidence.length === 0 ? (
              <p className="mt-2 text-sm text-gray-500">
                표시할 근거가 없습니다.
              </p>
            ) : (
              <div className="mt-2 space-y-2">
                {answer.evidence.map((evidence, index) => (
                  <div
                    key={`${evidence.page}-${index}`}
                    className="rounded-lg bg-gray-50 p-3"
                  >
                    <p className="text-xs font-medium text-gray-500">
                      {evidence.page ? `${evidence.page}p` : "페이지 없음"}
                      {evidence.section ? ` · ${evidence.section}` : ""}
                    </p>

                    <p className="mt-2 text-sm leading-6 text-gray-700">
                      {evidence.text || "-"}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
};

export default QuestionBox;