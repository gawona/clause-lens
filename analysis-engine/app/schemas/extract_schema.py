from pydantic import BaseModel, Field


class PdfExtractRequest(BaseModel):
    document_id: str = Field(..., description="Spring Boot documents 테이블의 documentId")
    file_path: str = Field(..., description="분석할 PDF 파일 경로")


class PdfPageText(BaseModel):
    page: int
    text: str


class PdfExtractResponse(BaseModel):
    document_id: str
    page_count: int
    pages: list[PdfPageText]