package com.guideon.service;

import com.guideon.config.ConfigLoader;
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
    private final Analyzer analyzer;
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
        this.analyzer = LuceneAnalyzerFactory.createKoreanAnalyzer();

        // IndexWriter 초기화
        initializeIndexWriter();

        logger.info("BM25SearchService initialized: path={}, k1={}, b={}", indexPath, k1, b);
    }

    /**
     * IndexWriter 초기화
     */
    private void initializeIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
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
        doc.add(new StringField("regulation_type", regulationType, Field.Store.YES));

        indexWriter.addDocument(doc);

        logger.debug("Indexed segment: id={}, type={}, contentLength={}",
                segmentId, regulationType, segment.text().length());
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

        // QueryParser로 쿼리 파싱
        QueryParser parser = new QueryParser("content", analyzer);
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
