package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.DocumentMetadata;
import com.guideon.model.ScoredSegment;
import com.guideon.util.LuceneAnalyzerFactory;
import com.guideon.util.SearchResultConverter;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * BM25 기반 키워드 검색 서비스
 * Apache Lucene을 사용한 전문 검색
 */
public class BM25SearchService {
    private static final Logger logger = LoggerFactory.getLogger(BM25SearchService.class);

    private final Directory directory;
    private final Analyzer indexingAnalyzer;  // 인덱싱용
    private final Analyzer searchAnalyzer;    // 검색용
    private IndexWriter indexWriter;
    private DirectoryReader indexReader;
    private IndexSearcher indexSearcher;

    private final String indexPath;
    private final float k1;
    private final float b;

    /**
     * ConfigLoader 기반 생성자
     */
    public BM25SearchService(ConfigLoader config) throws IOException {
        this.indexPath = config.getBM25IndexDirectory();
        this.k1 = (float) config.getBM25K1();
        this.b = (float) config.getBM25B();

        // 인덱스 디렉토리 생성
        Path path = Paths.get(indexPath);
        Files.createDirectories(path);

        this.directory = FSDirectory.open(path);

        // Phase 4.1: 인덱싱과 검색에 서로 다른 Analyzer 사용
        this.indexingAnalyzer = LuceneAnalyzerFactory.createKoreanAnalyzer();  // 강력한 필터링 (불용어 제거)
        this.searchAnalyzer = LuceneAnalyzerFactory.createSearchQueryAnalyzer(); // 완화된 필터링 (재현율 향상)

        // IndexWriter 초기화
        initializeIndexWriter();

        logger.info("BM25SearchService initialized: path={}, k1={}, b={}", indexPath, k1, b);
        logger.info("  - Indexing Analyzer: EnhancedKoreanAnalyzer (with stopwords)");
        logger.info("  - Search Analyzer: SearchQueryAnalyzer (minimal filtering)");
    }

    /**
     * IndexWriter 초기화
     */
    private void initializeIndexWriter() throws IOException {
        // 인덱싱에는 indexingAnalyzer 사용 (강력한 필터링)
        IndexWriterConfig config = new IndexWriterConfig(indexingAnalyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        config.setSimilarity(new BM25Similarity(k1, b));
        config.setRAMBufferSizeMB(256);

        this.indexWriter = new IndexWriter(directory, config);
    }

    /**
     * 문서 인덱싱
     *
     * @param segment TextSegment
     * @param regulationType 규정 유형
     * @param segmentId 세그먼트 ID
     */
    public void indexSegment(TextSegment segment, String regulationType, String segmentId) throws IOException {
        Document doc = new Document();

        // ID 필드 (저장 + 검색 불가)
        doc.add(new StringField("id", segmentId, Field.Store.YES));

        // 내용 필드 (저장 + 검색 가능)
        doc.add(new TextField("content", segment.text(), Field.Store.YES));

        // 규정 유형 (저장 + 필터링 가능)
        doc.add(new StringField(DocumentMetadata.REGULATION_TYPE, regulationType, Field.Store.YES));

        // 문서 ID 필드 추가 (삭제를 위해 필요)
        String documentId = segment.metadata().getString(DocumentMetadata.DOCUMENT_ID);
        if (documentId != null) {
            doc.add(new StringField(DocumentMetadata.DOCUMENT_ID, documentId, Field.Store.YES));
        }

        String filename = segment.metadata().getString(DocumentMetadata.FILENAME);
        if (filename != null) {
            doc.add(new StringField(DocumentMetadata.FILENAME, filename, Field.Store.YES));
        }

        indexWriter.addDocument(doc);

        logger.debug("Indexed segment: id={}, type={}, documentId={}, contentLength={}",
                segmentId, regulationType, documentId, segment.text().length());
    }

    /**
     * BM25 검색 수행
     *
     * @param query 검색 쿼리
     * @param maxResults 최대 결과 수
     * @return 검색 결과 리스트
     */
    public List<ScoredSegment> search(String query, int maxResults) throws IOException, ParseException {
        // IndexReader 갱신
        refreshIndexReader();

        // QueryParser로 쿼리 파싱 (검색에는 searchAnalyzer 사용 - 완화된 필터링)
        QueryParser parser = new QueryParser("content", searchAnalyzer);
        Query luceneQuery = parser.parse(query);

        // 검색 수행
        TopDocs topDocs = indexSearcher.search(luceneQuery, maxResults);

        // 결과 변환
        List<ScoredSegment> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = indexReader.storedFields().document(scoreDoc.doc);
            TextSegment segment = SearchResultConverter.toTextSegment(doc);
            String id = SearchResultConverter.extractId(doc);

            ScoredSegment scoredSegment = new ScoredSegment(
                    id,
                    segment,
                    scoreDoc.score,
                    "BM25"
            );

            results.add(scoredSegment);
        }

        logger.debug("BM25 search completed: query='{}', results={}", query, results.size());
        return results;
    }

    /**
     * IndexReader 갱신 (검색 전 필수)
     */
    private void refreshIndexReader() throws IOException {
        if (indexReader == null) {
            indexReader = DirectoryReader.open(indexWriter);
            indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new BM25Similarity(k1, b));
        } else {
            DirectoryReader newReader = DirectoryReader.openIfChanged(indexReader);
            if (newReader != null) {
                indexReader.close();
                indexReader = newReader;
                indexSearcher = new IndexSearcher(indexReader);
                indexSearcher.setSimilarity(new BM25Similarity(k1, b));
            }
        }
    }

    /**
     * 인덱스 커밋
     */
    public void commit() throws IOException {
        if (indexWriter != null) {
            indexWriter.commit();
            logger.info("BM25 index committed");
        }
    }

    /**
     * 인덱스 초기화 (모든 문서 삭제)
     */
    public void clearIndex() throws IOException {
        if (indexWriter != null) {
            indexWriter.deleteAll();
            indexWriter.commit();
            logger.info("BM25 index cleared");
        }
    }

    /**
     * 특정 문서의 모든 세그먼트 삭제
     * BM25 인덱스에서 해당 document_id를 가진 모든 세그먼트를 삭제합니다.
     *
     * @param documentId 삭제할 문서 ID
     * @return 삭제된 세그먼트 수
     */
    public int deleteDocumentSegments(String documentId) throws IOException {
        logger.info("Deleting BM25 segments for document: {}", documentId);

        int deletedCount = 0;

        try {
            // IndexReader 갱신
            refreshIndexReader();

            // document_id로 검색하여 삭제할 문서 찾기
            Term term = new Term("document_id", documentId);
            long deletedLong = indexWriter.deleteDocuments(term);
            deletedCount = (int) deletedLong;

            logger.info("Deleted {} BM25 segments for document: {}", deletedCount, documentId);

        } catch (Exception e) {
            logger.error("Error deleting BM25 segments for document: {}", documentId, e);
            throw new IOException("BM25 인덱스에서 세그먼트 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return deletedCount;
    }

    /**
     * 인덱싱된 문서 수 반환
     */
    public int getDocumentCount() throws IOException {
        refreshIndexReader();
        return indexReader.numDocs();
    }

    /**
     * 리소스 정리
     */
    public void close() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (indexReader != null) {
            indexReader.close();
        }
        if (directory != null) {
            directory.close();
        }
        logger.info("BM25SearchService closed");
    }
}
