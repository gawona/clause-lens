import { useRef, useState } from "react";
import { uploadDocument } from "../../api/documentApi";
import type { DocumentItem, DocumentType } from "../../types/document";

interface DocumentUploadCardProps {
  onUploaded: (document: DocumentItem) => void;
}

const documentTypeOptions: { label: string; value: DocumentType }[] = [
  { label: "계약서", value: "CONTRACT" },
  { label: "약관", value: "TERMS" },
  { label: "공고문", value: "NOTICE" },
  { label: "정책 문서", value: "POLICY" },
  { label: "업무 문서", value: "BUSINESS_DOCUMENT" },
];

const DocumentUploadCard = ({ onUploaded }: DocumentUploadCardProps) => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [documentName, setDocumentName] = useState("");
  const [documentType, setDocumentType] = useState<DocumentType>("CONTRACT");
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];

    if (!selectedFile) {
      return;
    }

    if (selectedFile.type !== "application/pdf") {
      alert("PDF 파일만 업로드할 수 있습니다.");
      event.target.value = "";
      setFile(null);
      return;
    }

    setFile(selectedFile);

    if (!documentName.trim()) {
      const nameWithoutExtension = selectedFile.name.replace(/\.pdf$/i, "");
      setDocumentName(nameWithoutExtension);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      alert("업로드할 PDF 파일을 선택해주세요.");
      return;
    }

    if (!documentName.trim()) {
      alert("문서명을 입력해주세요.");
      return;
    }

    try {
      setIsUploading(true);

      const uploadedDocument = await uploadDocument({
        file,
        documentName: documentName.trim(),
        documentType,
      });

      setDocumentName("");
      setDocumentType("CONTRACT");
      setFile(null);

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

      onUploaded(uploadedDocument);
    } catch (error) {
      console.error("문서 업로드 실패:", error);
      alert("문서 업로드 중 오류가 발생했습니다.");
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <section className="mb-8 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div className="mb-5">
        <h2 className="text-lg font-bold text-slate-900">PDF 문서 업로드</h2>
        <p className="mt-1 text-sm text-slate-500">
          분석할 PDF 문서를 업로드하고 문서 유형을 선택하세요.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1.2fr_0.8fr_1.2fr_auto]">
        <div>
          <label className="mb-2 block text-sm font-semibold text-slate-700">
            문서명
          </label>

          <input
            value={documentName}
            onChange={(event) => setDocumentName(event.target.value)}
            placeholder="예: 물품구매계약서"
            className="h-11 w-full rounded-xl border border-slate-300 px-4 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-semibold text-slate-700">
            문서 유형
          </label>

          <select
            value={documentType}
            onChange={(event) =>
              setDocumentType(event.target.value as DocumentType)
            }
            className="h-11 w-full rounded-xl border border-slate-300 px-4 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
          >
            {documentTypeOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-2 block text-sm font-semibold text-slate-700">
            PDF 파일
          </label>

          <input
            ref={fileInputRef}
            type="file"
            accept="application/pdf"
            onChange={handleFileChange}
            className="hidden"
          />

          <div className="flex h-11 items-center rounded-xl border border-slate-300 bg-white px-3">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="shrink-0 rounded-lg bg-slate-900 px-3 py-1.5 text-sm font-semibold text-white hover:bg-slate-800"
            >
              파일 선택
            </button>

            <span className="ml-3 truncate text-sm text-slate-500">
              {file ? file.name : "선택된 파일 없음"}
            </span>
          </div>
        </div>

        <div className="flex items-end">
          <button
            type="button"
            onClick={handleUpload}
            disabled={isUploading}
            className="h-11 w-full rounded-xl bg-blue-600 px-5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-300 lg:w-auto"
          >
            {isUploading ? "업로드 중..." : "업로드"}
          </button>
        </div>
      </div>

      {file && (
        <div className="mt-4 rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-600">
          선택된 파일: <span className="font-semibold">{file.name}</span>
        </div>
      )}
    </section>
  );
};

export default DocumentUploadCard;