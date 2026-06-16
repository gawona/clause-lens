import { useState } from "react";
import { askQuestion } from "../../api/documentApi";
import type { QuestionResponse } from "../../types/document";
import MarkdownViewer from "../common/MarkdownViewr";

interface QuestionBoxProps {
  documentId: string;
  disabled?: boolean;
}

const questionExamples = [
  "계약금액은 얼마야?",
  "납품기한은 언제까지야?",
  "지체상금률은 얼마야?",
  "대가의 지급 조건을 정리해줘.",
  "계약 해제 또는 해지 조건을 알려줘.",
  "위험 조항을 요약해줘.",
];

const QuestionBox = ({ documentId, disabled = false }: QuestionBoxProps) => {
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState<QuestionResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const executeQuestion = async (targetQuestion: string) => {
    if (disabled) {
      return;
    }

    if (!targetQuestion.trim()) {
      return;
    }

    try {
      setIsLoading(true);
      setErrorMessage(null);

      const result = await askQuestion(documentId, targetQuestion.trim());

      setQuestion(targetQuestion);
      setAnswer(result);
    } catch (error) {
      console.error("질의응답 실패:", error);
      setErrorMessage("질의응답 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleAskQuestion = async () => {
    await executeQuestion(question);
  };

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h3 className="text-lg font-bold text-slate-900">
            문서 기반 질의응답
          </h3>
          <p className="mt-1 text-sm text-slate-500">
            분석된 문서 내용을 기반으로 질문하고, 답변과 근거를 확인합니다.
          </p>
        </div>
      </div>

      {disabled && (
        <div className="mt-4 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
          문서 분석 완료 후 질문할 수 있습니다.
        </div>
      )}

      {!disabled && (
        <div className="mt-4 flex flex-wrap gap-2">
          {questionExamples.map((example) => (
            <button
              key={example}
              type="button"
              onClick={() => executeQuestion(example)}
              disabled={isLoading}
              className="rounded-full border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:bg-slate-100"
            >
              {example}
            </button>
          ))}
        </div>
      )}

      <div className="mt-4 flex gap-2">
        <input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !disabled) {
              handleAskQuestion();
            }
          }}
          disabled={disabled}
          placeholder={
            disabled
              ? "문서 분석 완료 후 질문할 수 있습니다."
              : "예: 이 계약서에서 위약금 조항 찾아줘"
          }
          className="flex-1 rounded-xl border border-slate-300 px-4 py-2 text-sm outline-none focus:border-blue-500 disabled:bg-slate-100 disabled:text-slate-400"
        />

        <button
          type="button"
          onClick={handleAskQuestion}
          disabled={isLoading || disabled}
          className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {isLoading ? "질문 중..." : "질문"}
        </button>
      </div>

      {errorMessage && (
        <p className="mt-3 text-sm text-red-600">{errorMessage}</p>
      )}

      {answer && (
        <div className="mt-5 space-y-5">
          <div>
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-slate-900">답변</p>

              {answer.confidence !== undefined &&
                answer.confidence !== null && (
                  <span className="rounded-full bg-blue-50 px-2 py-1 text-xs font-medium text-blue-600">
                    confidence {answer.confidence}
                  </span>
                )}
            </div>

            <div className="mt-2 rounded-xl bg-slate-50 p-5">
              <MarkdownViewer content={answer.answer} />
            </div>
          </div>

          <div>
            <p className="text-sm font-semibold text-slate-900">근거</p>

            {!answer.evidence || answer.evidence.length === 0 ? (
              <p className="mt-2 text-sm text-slate-500">
                표시할 근거가 없습니다.
              </p>
            ) : (
              <div className="mt-2 space-y-3">
                {answer.evidence.map((evidence, index) => {
                  const evidenceText = evidence.text || evidence.content || "";

                  return (
                    <div
                      key={`${evidence.pageNumber}-${index}`}
                      className="rounded-xl bg-slate-50 p-4"
                    >
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-xs font-semibold text-slate-500">
                          {evidence.pageNumber
                            ? `${evidence.pageNumber}p`
                            : "페이지 없음"}
                          {evidence.sectionTitle
                            ? ` · ${evidence.sectionTitle}`
                            : ""}
                        </p>

                        {evidence.score !== undefined &&
                          evidence.score !== null && (
                            <span className="text-xs text-slate-400">
                              score {evidence.score.toFixed(2)}
                            </span>
                          )}
                      </div>

                      <p className="mt-2 whitespace-pre-line text-sm leading-6 text-slate-700">
                        {evidenceText || "-"}
                      </p>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
};

export default QuestionBox;