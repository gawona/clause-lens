import { useState } from "react";
import ReactMarkdown from "react-markdown"
import { askQuestion } from "../../api/documentApi";
import type { QuestionResponse } from "../../types/document";
import MarkdownViewer from "../common/MarkdownViewr";

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
        <div className="mt-5 space-y-5">
          <div>
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-slate-900">답변</p>

              {answer.confidence !== undefined && answer.confidence !== null && (
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