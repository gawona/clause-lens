export type DocumentType =
  | "CONTRACT"
  | "TERMS"
  | "NOTICE"
  | "POLICY"
  | "BUSINESS_DOCUMENT";

export type UploadStatus = "UPLOADED" | "FAILED";

export type AnalysisStatus =
  | "UPLOADED"
  | "EXTRACTING"
  | "CHUNKING"
  | "INDEXING"
  | "ANALYZING"
  | "COMPLETED"
  | "FAILED";

export interface DocumentItem {
  documentId: string;
  documentName: string;
  originalFilename: string;
  fileSize: number;
  contentType: string;
  documentType: DocumentType;
  uploadStatus: UploadStatus;
  analysisStatus: AnalysisStatus;
  createdAt: string;
}

export interface DocumentDetail extends DocumentItem {}

export interface ContractExtraction {
  contractTitle?: string | null;
  parties?: string | null;
  contractAmount?: string | number | null;
  startDate?: string | null;
  endDate?: string | null;
  paymentTerms?: string | null;
  penaltyClause?: string | null;
  terminationClause?: string | null;
  [key: string]: unknown;
}

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export interface RiskEvidence {
  page?: number | null;
  section?: string | null;
  text?: string | null;
}

export interface RiskItem {
  riskLevel?: RiskLevel;
  riskType?: string;
  reason?: string;
  evidence?: RiskEvidence | null;
  recommendation?: string | null;
}

export interface QuestionResponse {
  answer: string;
  confidence?: number | null;
  evidence?: {
    page?: number | null;
    section?: string | null;
    text?: string | null;
  }[];
}