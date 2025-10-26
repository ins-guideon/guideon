# 📄 문서 업로드 및 벡터 인덱싱 가이드

## 🎯 기능 개요
PDF, DOC, DOCX, TXT 파일을 업로드하여 자동으로 파싱하고 벡터 임베딩을 생성하여 데이터베이스에 인덱싱하는 기능입니다.

## 📋 구현 상태

### ✅ 백엔드 (완료)
- **문서 파싱**: Apache PDFBox (PDF), Apache POI (DOC/DOCX)
- **벡터 임베딩**: all-MiniLM-L6-v2 모델
- **저장소**: InMemoryEmbeddingStore (향후 Qdrant로 교체 가능)
- **청크 분할**: 500자 단위, 100자 오버랩

**주요 파일:**
- 서비스: [DocumentService.java](src/main/java/com/guideon/service/DocumentService.java)
- 컨트롤러: [DocumentController.java](src/main/java/com/guideon/controller/DocumentController.java)
- 벡터 인덱싱: [RegulationSearchService.java:100](src/main/java/com/guideon/service/RegulationSearchService.java#L100)

### ✅ 프론트엔드 (완료)
- **업로드 UI**: Ant Design Upload 컴포넌트
- **파일 검증**: 타입, 크기 체크
- **문서 목록**: 인덱싱된 문서 관리

**주요 파일:**
- 페이지: [DocumentUpload.tsx](guideon-frontend/src/pages/DocumentUpload.tsx)
- 서비스: [documentService.ts](guideon-frontend/src/services/documentService.ts)
- 라우트: `/documents`

## 🚀 API 엔드포인트

### 1. POST /api/documents/upload
문서 업로드 및 인덱싱

**요청:**
- Content-Type: `multipart/form-data`
- Parameters:
  - `file`: 업로드할 파일 (PDF, DOC, DOCX, TXT)
  - `regulationType`: 규정 유형 (예: "출장여비지급규정")

**응답:**
```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "fileName": "출장여비지급규정.pdf",
    "regulationType": "출장여비지급규정",
    "fileSize": 1048576,
    "uploadTimestamp": 1234567890000,
    "status": "indexed",
    "message": "문서가 성공적으로 업로드되고 인덱싱되었습니다."
  }
}
```

### 2. GET /api/documents
인덱싱된 문서 목록 조회

**응답:**
```json
{
  "success": true,
  "data": {
    "documents": [
      {
        "id": "uuid-string",
        "fileName": "출장여비지급규정.pdf",
        "regulationType": "출장여비지급규정",
        "fileSize": 1048576,
        "uploadTimestamp": 1234567890000,
        "status": "indexed"
      }
    ],
    "totalCount": 1
  }
}
```

### 3. DELETE /api/documents/{id}
문서 삭제

**응답:**
```json
{
  "success": true,
  "message": "문서가 성공적으로 삭제되었습니다."
}
```

## 📝 사용 방법

### 1. 백엔드 서버 시작
```bash
./run-server.bat
# 또는
mvn spring-boot:run
```

### 2. 프론트엔드 개발 서버 시작
```bash
cd guideon-frontend
npm install
npm run dev
```

### 3. 브라우저에서 접속
1. http://localhost:3000/documents 접속
2. 규정 유형 선택 (드롭다운)
3. 파일 선택 (PDF, DOC, DOCX, TXT)
4. "업로드 및 인덱싱" 버튼 클릭

### 4. 처리 과정
1. **파일 검증**: 파일 타입, 크기 확인
2. **파일 저장**: `~/guideon/uploads/` 디렉토리에 저장
3. **문서 파싱**: 텍스트 추출
4. **청크 분할**: 500자 단위로 분할 (100자 오버랩)
5. **임베딩 생성**: 각 청크를 벡터로 변환
6. **벡터 저장**: EmbeddingStore에 저장
7. **메타데이터 저장**: 문서 정보 기록

## 🧪 테스트 시나리오

### 시나리오 1: PDF 문서 업로드
```
1. 규정 유형: "출장여비지급규정"
2. 파일: 출장여비지급규정.pdf (예시)
3. 결과: 문서가 파싱되고 벡터 DB에 인덱싱됨
4. 확인: /qa 페이지에서 "해외 출장 숙박비는?" 질문 시 해당 문서 참조
```

### 시나리오 2: 여러 문서 업로드
```
1. 출장여비지급규정.pdf
2. 윤리규정.docx
3. 취업규칙.txt
4. 결과: 각 문서가 해당 규정 유형으로 분류되어 인덱싱됨
```

### 시나리오 3: 문서 삭제
```
1. 문서 목록에서 삭제할 문서 선택
2. "삭제" 버튼 클릭
3. 확인 다이얼로그에서 "삭제" 클릭
4. 결과: 파일 및 벡터 데이터 삭제
```

## ⚙️ 설정

### 파일 크기 제한
현재 제한: **10MB**

수정 방법:
1. 백엔드: [DocumentService.java:116](src/main/java/com/guideon/service/DocumentService.java#L116)
2. 프론트엔드: [DocumentUpload.tsx:99](guideon-frontend/src/pages/DocumentUpload.tsx#L99)
3. Spring Boot 설정: [application.yml:13](src/main/resources/application.yml#L13)

### 청크 크기 및 오버랩
현재 설정:
- 청크 크기: 500자
- 오버랩: 100자

수정 방법: [application.yml:42-44](src/main/resources/application.yml#L42)
```yaml
rag:
  chunk:
    size: 500
    overlap: 100
```

### 임베딩 모델
현재 모델: **all-MiniLM-L6-v2**
- 차원: 384
- 속도: 빠름
- 정확도: 중간

다른 모델로 변경:
- [RegulationSearchService.java:55](src/main/java/com/guideon/service/RegulationSearchService.java#L55)

### 저장 경로
업로드된 파일 저장 경로: `~/guideon/uploads/`

수정 방법:
- [DocumentService.java:37](src/main/java/com/guideon/service/DocumentService.java#L37)

## 🔍 벡터 검색 동작 방식

### 1. 인덱싱 과정
```
문서 업로드 → 파싱 → 청크 분할 → 임베딩 생성 → 벡터 저장
```

### 2. 검색 과정
```
질문 입력 → 질문 임베딩 생성 → 벡터 유사도 검색 → 상위 N개 검색 → RAG 답변 생성
```

### 3. 유사도 계산
- 코사인 유사도 사용
- 최소 점수: 0.5 (설정 가능)
- 최대 결과 수: 5개 (설정 가능)

## 🐛 트러블슈팅

### 문제 1: 업로드 실패 - "지원하지 않는 파일 형식"
**원인**: 허용되지 않은 파일 타입
**해결**: PDF, DOC, DOCX, TXT 파일만 사용

### 문제 2: 업로드 실패 - "파일 크기 초과"
**원인**: 10MB를 초과하는 파일
**해결**:
- 파일 크기 줄이기
- 또는 제한 설정 변경

### 문제 3: 인덱싱 후 검색이 안됨
**원인**:
- 임베딩 모델 로드 실패
- 벡터 저장소 초기화 실패

**해결**:
1. 백엔드 로그 확인
2. 임베딩 모델 다운로드 확인
3. 메모리 확인 (임베딩 모델은 메모리 필요)

### 문제 4: 한글 문서 파싱 오류
**원인**: 인코딩 문제
**해결**:
- UTF-8 인코딩으로 저장된 문서 사용
- PDF의 경우 텍스트 추출 가능한 PDF 사용 (이미지 PDF 불가)

### 문제 5: 메모리 부족
**원인**: 대용량 문서 또는 많은 문서 인덱싱
**해결**:
- JVM 힙 메모리 증가: `-Xmx2g`
- 청크 크기 증가하여 세그먼트 수 감소
- 배치 처리로 분할 업로드

## 🚀 향후 개선 사항

### 1. Qdrant 연동
현재는 InMemoryEmbeddingStore 사용 (서버 재시작시 초기화됨)

**Qdrant 연동 방법:**
```java
// RegulationSearchService.java
QdrantClient client = new QdrantClient(QdrantGrpcClient.newBuilder(
    "localhost",
    6334,
    false
).build());

this.embeddingStore = QdrantEmbeddingStore.builder()
    .client(client)
    .collectionName("regulations")
    .build();
```

### 2. 데이터베이스 저장
현재는 메모리에 메타데이터 저장

**개선 방안:**
- JPA Entity 생성
- PostgreSQL/MySQL 연동
- 영구 저장

### 3. 비동기 처리
현재는 동기 처리 (업로드 시 대기)

**개선 방안:**
- Spring @Async 사용
- 작업 큐 (RabbitMQ, Kafka)
- 진행 상태 실시간 업데이트 (WebSocket)

### 4. 문서 미리보기
**기능:**
- PDF 뷰어 통합
- 검색 결과 하이라이팅
- 원본 문서 다운로드

### 5. OCR 지원
이미지 기반 PDF 처리

**라이브러리:**
- Tesseract OCR
- Google Vision API

### 6. 다국어 지원
**개선 방안:**
- 다국어 임베딩 모델
- 언어별 토크나이저
- 번역 API 통합

## 📊 성능 최적화

### 임베딩 생성 속도
- all-MiniLM-L6-v2: ~50ms/텍스트
- 배치 처리로 속도 향상 가능

### 메모리 사용량
- 모델 로딩: ~100MB
- 문서당 평균: ~1-5MB (청크 수에 따라)

### 권장 사양
- CPU: 4코어 이상
- RAM: 4GB 이상
- 디스크: 10GB 여유 공간

## ✅ 체크리스트

- [ ] 백엔드 서버 실행 확인
- [ ] 프론트엔드 서버 실행 확인
- [ ] /documents 페이지 접속 가능
- [ ] 규정 유형 선택 가능
- [ ] 파일 업로드 가능
- [ ] 업로드 진행 상태 표시
- [ ] 문서 목록 표시
- [ ] 문서 삭제 가능
- [ ] /qa 페이지에서 업로드한 문서 기반 답변 생성 확인

## 📚 참고 자료

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [all-MiniLM-L6-v2 Model](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2)
- [Qdrant Vector Database](https://qdrant.tech/)

---

**완료!** 이제 PDF 문서를 업로드하여 벡터 데이터베이스를 구성할 수 있습니다. 🎉
