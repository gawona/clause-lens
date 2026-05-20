from pathlib import Path

import fitz

from app.schemas.extract_schema import PdfPageText, PdfExtractResponse


class PdfExtractService:

    def extract_text(self, document_id: str, file_path: str) -> PdfExtractResponse:
        pdf_path = Path(file_path)

        if not pdf_path.exists():
            raise FileNotFoundError(f"PDF 파일을 찾을 수 없습니다. file_path={file_path}")

        pages: list[PdfPageText] = []

        with fitz.open(pdf_path) as document:
            for page_index in range(document.page_count):
                page = document.load_page(page_index)
                text = page.get_text("text")

                pages.append(
                    PdfPageText(
                        page=page_index + 1,
                        text=text.strip()
                    )
                )

            return PdfExtractResponse(
                document_id=document_id,
                page_count=document.page_count,
                pages=pages
            )


extract_service = PdfExtractService()