from fastapi import APIRouter, HTTPException

from app.schemas.extract_schema import PdfExtractRequest, PdfExtractResponse
from app.services.extract_service import extract_service

router = APIRouter(
    prefix="/api/pdf",
    tags=["PDF"]
)


@router.post("/extract", response_model=PdfExtractResponse)
def extract_text(request: PdfExtractRequest):
    try:
        return extract_service.extract_text(
            document_id=request.document_id,
            file_path=request.file_path
        )
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"PDF 텍스트 추출 중 오류가 발생했습니다. detail={str(e)}")