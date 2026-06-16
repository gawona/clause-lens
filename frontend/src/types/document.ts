export type DocumentType =
  | "CONTRACT"
  | "TERMS"
  | "NOTICE"
  | "POLICY"
  | "BUSINESS_DOCUMENT";

export type UploadStatus = "UPLOADED" | "FAILED";

export type AnalysisStatus =
  | "NOT_STARTED"
  | "REQUESTED"
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

export interface DocumentUploadRequest {
  documentName: string;
  documentType: DocumentType;
  file: File;
}

export interface ExtractionEvidence {
  chunkId: number;
  pageNumber: number;
  sectionTitle: string | null;
  text: string;
  score: number;
}

export interface ContractExtraction {
  contractTitle: string | null;
  parties: string[];
  contractAmount: string | null;
  contractPeriod: string | null;
  deliveryDeadline: string | null;
  paymentTerms: string | null;
  penaltyClause: string | null;
  terminationClause: string | null;
  evidence: ExtractionEvidence[];
}

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export interface RiskEvidence {
  pageNumber?: number | null;
  sectionTitle?: string | null;
  text?: string | null;
}

export interface RiskItem {
  riskLevel?: RiskLevel;
  riskType?: string;
  reason?: string;
  evidence?: RiskEvidence | null;
  recommendation?: string | null;
}

export interface QuestionEvidence {
  chunkId?: number;
  pageNumber?: number | null;
  sectionTitle?: string | null;
  text?: string | null;
  content?: string | null;
  score?: number | null;
}

export interface QuestionResponse {
  answer: string;
  confidence?: number | null;
  evidence?: QuestionEvidence[];
}