# ClauseLens 🔍✨

계약서·약관·정책 문서의 핵심 항목 추출, 근거 기반 질의응답, 위험 조항 탐지를 제공하는 RAG 기반 AI 문서 분석 플랫폼입니다.

ClauseLens는 PDF 문서를 업로드하면 텍스트를 추출하고, 문서를 검색 가능한 chunk 단위로 분리한 뒤 OpenSearch에 색인합니다. 이후 사용자의 질문에 대해 관련 chunk를 검색하고, LLM을 통해 근거 기반 답변, 핵심 항목 JSON 추출, 위험 조항 탐지를 수행합니다.

---

## 주요 기능

#### BackEnd

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

#### FrontEnd

- React 기반 문서 목록 화면 구현
- PDF 업로드 화면 구현 
- 업로드 후 신규 문서 상세 화면 자동 이동 
- 문서 상세 분석 결과 화면 구현 
- 분석 상태별 버튼 제어 
- 핵심 항목 추출 결과 테이블 표시 
- 위험 조항 탐지 결과 카드 표시 
- RAG 질의응답 UI 구현 
- AI 답변 Markdown 렌더링 
- 근거 문장, 페이지, section, score 표시 
- 질문 예시 버튼 제공 
- 분석 전/분석 중/분석 완료 상태별 사용자 안내 처리

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

### 10. React 기반 문서 목록 화면

업로드된 문서 목록을 조회하고, 문서명, 파일명, 문서 유형, 업로드 상태, 분석 상태, 생성일을 화면에 표시합니다.

- 문서 목록 조회 API 연동
- 문서 상태 뱃지 표시
- 문서 상세 화면 이동 처리
- 분석 상태별 UI 표시

### 11. PDF 업로드 화면

사용자가 PDF 파일을 선택하고 문서명과 문서 유형을 입력하여 문서를 업로드할 수 있는 화면을 구현했습니다.

- PDF 파일 선택 UI 구현
- 문서명 자동 입력 처리
- 문서 유형 선택 처리
- multipart/form-data 기반 업로드 API 연동
- 업로드 성공 후 신규 문서 상세 화면으로 자동 이동
- PDF 파일 형식 검증

### 12. 문서 상세 분석 결과 화면

문서 상세 화면에서 문서 기본 정보, 핵심 항목 추출 결과, 위험 조항 탐지 결과, 문서 기반 질의응답을 한 화면에서 확인할 수 있도록 구성했습니다.

- 문서 메타데이터 카드 표시
- 분석 상태별 버튼 활성화/비활성화 처리
- NOT_STARTED 문서 분석 실행 버튼 제공
- COMPLETED 문서 분석 완료 상태 표시
- 분석 실행 중 안내 메시지 표시
- 분석 실패 시 에러 메시지 표시

### 13. 핵심 항목 추출 결과 UI

LLM이 추출한 계약 핵심 정보를 테이블 형태로 표시하고, 관련 근거 문장을 함께 확인할 수 있도록 구현했습니다.

- 계약서 제목, 계약 당사자, 계약 금액 표시
- 계약 기간, 납품 기한, 지급 조건 표시
- 지체상금 조항, 해지 조항 표시
- 근거 문장 및 페이지 번호 표시
- score 기반 근거 정보 표시
- 긴 근거 문장 접기/펼치기 처리

### 14. 위험 조항 탐지 결과 UI

탐지된 위험 조항을 카드 형태로 표시하고, 위험 수준에 따라 시각적으로 구분했습니다.

- HIGH, MEDIUM, LOW 위험 수준 표시
- 위험 수준별 정렬 처리
- 위험 유형, 판단 사유, 근거 문장 표시
- 개선 권고사항 표시
- 위험 수준별 요약 카운트 표시

### 15. 문서 기반 질의응답 UI

사용자가 문서에 대해 질문하면 RAG 기반 답변과 근거를 화면에서 확인할 수 있도록 구현했습니다.

- 질문 입력창 구현
- 질문 예시 버튼 제공
- 질문 API 연동
- AI 답변 표시
- Markdown 기반 답변 렌더링
- 근거 문장, 페이지 번호, section, score 표시
- 분석 완료 전 질문창 비활성화 처리
- 질문 실패 시 에러 메시지 표시


---

## 기술적 특징

#### BackEnd
- PDF 업로드부터 AI 분석 결과 저장까지 이어지는 문서 분석 파이프라인 구현
- Spring Boot와 FastAPI를 분리하여 백엔드 API 서버와 AI 분석 서버의 역할 분리
- PyMuPDF를 활용한 PDF 페이지별 텍스트 추출
- 문단, 길이, 조항 패턴 기반 chunking 구조 구현
- PostgreSQL에는 원본 분석 데이터 저장, OpenSearch에는 검색용 chunk 색인
- OpenSearch BM25 검색 기반 RAG 질의응답 구조 구현
- LLM 응답에 근거 문장과 페이지 번호를 포함하여 답변 신뢰성 강화
- 핵심 항목 추출 결과와 위험 조항 탐지 결과를 JSON 형태로 구조화
- Docker Compose 기반으로 PostgreSQL, OpenSearch, Backend, AI Server 실행 환경 구성

#### FrontEnd
- React와 TypeScript 기반으로 문서 업로드, 목록, 상세, 질의응답 화면 구현 
- Tailwind CSS를 활용하여 분석 상태, 위험 수준, 근거 정보를 시각적으로 구분 
- Axios 기반 API 모듈을 분리하여 문서 조회, 업로드, 분석 실행, 질의응답 API 연동 
- React Router를 활용하여 문서 목록 화면과 상세 화면 라우팅 구현 
- 업로드 성공 후 생성된 documentId를 기반으로 신규 문서 상세 화면 자동 이동 처리 
- 분석 상태에 따라 버튼 활성화, 질문창 비활성화, 안내 메시지 표시를 분기 처리 
- AI 답변을 Markdown 형태로 렌더링하여 가독성 있는 질의응답 화면 구성 
- 위험 조항을 HIGH, MEDIUM, LOW 기준으로 정렬하여 중요도 중심의 결과 확인 가능

---

## 기술 스택

### Frontend

- React
- TypeScript
- React Router 
- Axios 
- Zustand 
- Tailwind CSS 
- React Markdown 
- Remark GFM

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

## Frontend 화면 흐름 

```text 
문서 목록 화면 진입 
→ PDF 문서 업로드 
→ 신규 문서 상세 화면 자동 이동 
→ 전체 분석 실행 
→ 분석 상태 안내 표시 
→ 핵심 항목 추출 결과 확인 
→ 위험 조항 탐지 결과 확인 
→ 문서 기반 질의응답 수행 
→ 답변 근거 문장 및 페이지 확인
```

---

## 프로젝트 구조

```text
ClauseLens
|-- README.md
|-- analysis-engine
|   |-- app
|   |   |-- api
|   |   |-- clients
|   |   |-- core
|   |   |-- main.py
|   |   |-- schemas
|   |   `-- services
|   `-- requirements.txt
|-- backend
|   |-- HELP.md
|   |-- bin
|   |   |-- default
|   |   |-- generated-sources
|   |   |-- generated-test-sources
|   |   |-- main
|   |   `-- test
|   |-- build.gradle
|   |-- gradle
|   |   `-- wrapper
|   |-- gradlew
|   |-- gradlew.bat
|   |-- settings.gradle
|   |-- src
|   |   |-- main
|   |   `-- test
|   `-- uploads
|       `-- documents
|-- docker-compose.yml
|-- frontend
|   |-- README.md
|   |-- eslint.config.js
|   |-- index.html
|   |-- package-lock.json
|   |-- package.json
|   |-- public
|   |   |-- favicon.svg
|   |   `-- icons.svg
|   |-- src
|   |   |-- App.tsx
|   |   |-- api
|   |   |-- assets
|   |   |-- components
|   |   |-- index.css
|   |   |-- main.tsx
|   |   |-- pages
|   |   |-- types
|   |   `-- utils
|   |-- tsconfig.app.json
|   |-- tsconfig.json
|   |-- tsconfig.node.json
|   `-- vite.config.ts
`-- git_convention.txt

```