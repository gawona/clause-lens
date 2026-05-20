from fastapi import FastAPI

app = FastAPI(
    title="ClauseLens Analysis Engine",
    description="PDF text extraction, chunking, RAG, and document analysis engine",
    version="0.1.0",
)


@app.get("/health")
def health_check():
    return {"status": "ok"}