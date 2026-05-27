# ClauseLens 🔍✨

계약서·약관·정책 문서의 핵심 항목 추출, 근거 기반 질의응답, 위험 조항 탐지를 제공하는 RAG 기반 AI 문서 분석 플랫폼입니다.

ClauseLens는 PDF 문서를 업로드하면 텍스트를 추출하고, 문서를 검색 가능한 chunk 단위로 분리한 뒤 OpenSearch에 색인합니다. 이후 사용자의 질문에 대해 관련 chunk를 검색하고, LLM을 통해 근거 기반 답변, 핵심 항목 JSON 추출, 위험 조항 탐지를 수행합니다.

---

## 주요 기능

- PDF 문서 업로드
- 문서 메타데이터 저장
- PDF 페이지별 텍스트 추출
- 페이지별 원문 저장
- 문서 chunking
- chunk PostgreSQL 저장
- OpenSearch chunk 색인
- OpenSearch 기반 문서 검색
- RAG 기반 질의응답
- 근거 문장 및 페이지 표시
- 핵심 항목 JSON 추출
- 위험 조항 탐지
- Spring Boot ↔ FastAPI 연동
- Docker Compose 기반 인프라 구성

---

## 구현 기능 상세

### 1. PDF 문서 업로드

사용자가 분석할 PDF 문서를 업로드하면 문서명, 문서 유형, 업로드 상태, 분석 상태를 함께 저장합니다.

- PDF 파일 업로드 API 구현
- 문서 메타데이터 저장
- 문서 유형 관리
- 업로드 상태 및 분석 상태 관리

### 2. PDF 텍스트 추출

업로드된 PDF에서 페이지별 텍스트를 추출하고, 원본 페이지 번호를 유지하여 저장합니다.

- PyMuPDF 기반 텍스트 추출
- 페이지별 원문 텍스트 저장
- 페이지 번호 유지
- 텍스트 기반 PDF 우선 지원

### 3. 문서 Chunking

추출된 페이지 텍스트를 검색 가능한 chunk 단위로 분리합니다.

- 페이지 단위 텍스트 기반 chunk 생성
- 문단 기준 chunking
- 길이 기준 추가 분할
- 조항 패턴 감지 시 section metadata 저장
- chunk별 documentId, pageNumber, section, content 관리

### 4. PostgreSQL 저장

문서 분석 과정에서 생성되는 문서, 페이지, chunk, 분석 결과 데이터를 PostgreSQL에 저장합니다.

- 문서 메타데이터 저장
- 페이지별 원문 저장
- chunk 데이터 저장
- 핵심 항목 추출 결과 저장
- 위험 조항 탐지 결과 저장

### 5. OpenSearch 색인 및 검색

생성된 chunk를 OpenSearch에 색인하고, 사용자의 질문과 관련 있는 문서 조각을 검색합니다.

- OpenSearch index 생성
- chunk 색인 처리
- BM25 기반 전문 검색
- documentId 기반 검색 범위 제한
- pageNumber, section, content 기반 검색 결과 확인

### 6. RAG 기반 질의응답

사용자 질문에 대해 OpenSearch에서 관련 chunk를 검색하고, 검색 결과를 기반으로 LLM이 답변을 생성합니다.

- 질문 기반 관련 chunk 검색
- 검색 결과를 context로 구성
- OpenAI API 기반 답변 생성
- 문서에 없는 내용은 추측하지 않도록 제한
- 답변과 함께 근거 정보 제공

### 7. 근거 문장 및 페이지 표시

AI 답변에 사용된 근거 문장과 원본 PDF 페이지 번호를 함께 제공합니다.

- evidence text 제공
- pageNumber 제공
- section 정보 제공
- 사용자가 답변 근거를 확인할 수 있는 구조 구현

### 8. 핵심 항목 JSON 추출

문서에서 주요 정보를 구조화된 JSON 형태로 추출합니다.

- 계약명
- 계약 당사자
- 계약금액
- 계약기간
- 지급 조건
- 지체상금 조항
- 해지 조건
- 기타 핵심 조항

추출 결과는 문서에 없는 내용을 임의 생성하지 않고, 확인되지 않는 값은 null로 처리하는 방식으로 구성했습니다.

### 9. 위험 조항 탐지

문서 내 위험하거나 불명확한 조항을 탐지하고, 위험 수준과 개선 권고를 제공합니다.

- 계약금액 누락 여부 탐지
- 계약기간 누락 여부 탐지
- 지체상금 과도 여부 탐지
- 검수 기준 불명확 여부 탐지
- 해지 조건 불명확 여부 탐지
- 손해배상 범위 과도 여부 탐지
- 저작권 귀속 불명확 여부 탐지
- 개인정보 처리 조항 누락 여부 탐지

위험 조항은 riskLevel, riskType, reason, evidence, recommendation 형태로 구조화하여 저장합니다.

---

## 기술적 특징

- PDF 업로드부터 AI 분석 결과 저장까지 이어지는 문서 분석 파이프라인 구현
- Spring Boot와 FastAPI를 분리하여 백엔드 API 서버와 AI 분석 서버의 역할 분리
- PyMuPDF를 활용한 PDF 페이지별 텍스트 추출
- 문단, 길이, 조항 패턴 기반 chunking 구조 구현
- PostgreSQL에는 원본 분석 데이터 저장, OpenSearch에는 검색용 chunk 색인
- OpenSearch BM25 검색 기반 RAG 질의응답 구조 구현
- LLM 응답에 근거 문장과 페이지 번호를 포함하여 답변 신뢰성 강화
- 핵심 항목 추출 결과와 위험 조항 탐지 결과를 JSON 형태로 구조화
- Docker Compose 기반으로 PostgreSQL, OpenSearch, Backend, AI Server 실행 환경 구성

---

## 기술 스택

### Frontend

- React
- TypeScript
- Zustand
- Tailwind CSS

### Backend

- Spring Boot
- Spring Data JPA
- PostgreSQL

### AI Server

- FastAPI
- PyMuPDF
- OpenAI API
- Pydantic

### Search

- OpenSearch
- BM25 Full-text Search

### Infra

- Docker Compose

---

## 시스템 흐름

```text
PDF 업로드
→ 문서 메타데이터 저장
→ FastAPI 분석 서버 호출
→ PDF 페이지별 텍스트 추출
→ 페이지별 원문 저장
→ 문서 chunking
→ chunk PostgreSQL 저장
→ OpenSearch 색인
→ 검색 API 호출
→ RAG 질의응답
→ 핵심 항목 JSON 추출
→ 위험 조항 탐지
→ 분석 결과 저장
```

---

## 프로젝트 구조

```text
ClauseLens
├── frontend        # React 기반 사용자 화면
├── backend         # Spring Boot API 서버
├── analysis-engine # FastAPI 기반 AI 분석 서버
└── docker-compose.yml
```