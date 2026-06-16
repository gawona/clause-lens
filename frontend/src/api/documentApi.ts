import { axiosInstance } from "./axiosInstance";
import type {
  ContractExtraction,
  DocumentDetail,
  DocumentItem,
  DocumentType,
  QuestionResponse,
  RiskItem,
} from "../types/document";

export const getDocuments = async (): Promise<DocumentItem[]> => {
  const response = await axiosInstance.get("/api/documents");
  return response.data;
};

export const getDocumentDetail = async (
  documentId: string
): Promise<DocumentDetail> => {
  const response = await axiosInstance.get(`/api/documents/${documentId}`);
  return response.data;
};

export const getContractExtraction = async (
  documentId: string
): Promise<ContractExtraction> => {
  const response = await axiosInstance.get(
    `/api/documents/${documentId}/extractions/contract`
  );
  return response.data;
};

export const getRisks = async (documentId: string): Promise<RiskItem[]> => {
  const response = await axiosInstance.get<{
    documentId: string;
    risks: RiskItem[];
  }>(`/api/documents/${documentId}/risks`);

  return response.data.risks;
};

export const askQuestion = async (
  documentId: string,
  question: string
): Promise<QuestionResponse> => {
  const response = await axiosInstance.post(
    `/api/documents/${documentId}/questions`,
    {
      question,
    }
  );

  return response.data;
};

export const runAnalysis = async (documentId: string): Promise<void> => {
  await axiosInstance.post(`/api/documents/${documentId}/analysis/run`);
};

export const uploadDocument = async (request: {
  documentName: string;
  documentType: DocumentType;
  file: File;
}): Promise<DocumentItem> => {
  const formData = new FormData();

  formData.append("file", request.file);
  formData.append("documentName", request.documentName);
  formData.append("documentType", request.documentType);

  const response = await axiosInstance.post<DocumentItem>(
    "/api/documents",
    formData
  );

  return response.data;
};