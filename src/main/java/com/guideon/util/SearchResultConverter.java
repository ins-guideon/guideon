package com.guideon.util;

import com.guideon.model.ScoredSegment;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.apache.lucene.document.Document;

/**
 * 검색 결과 변환 유틸리티
 * Lucene Document, EmbeddingMatch, ScoredSegment 간 변환
 */
public class SearchResultConverter {

    /**
     * Lucene Document를 TextSegment로 변환
     *
     * @param doc Lucene Document
     * @return TextSegment
     */
    public static TextSegment toTextSegment(Document doc) {
        String id = doc.get("id");
        String content = doc.get("content");
        String regulationType = doc.get("regulation_type");

        TextSegment segment = TextSegment.from(content);

        // 메타데이터 추가
        if (regulationType != null) {
            segment.metadata().put("regulation_type", regulationType);
        }
        if (id != null) {
            segment.metadata().put("id", id);
        }

        return segment;
    }

    /**
     * EmbeddingMatch를 ScoredSegment로 변환
     *
     * @param match EmbeddingMatch
     * @return ScoredSegment
     */
    public static ScoredSegment toScoredSegment(EmbeddingMatch<TextSegment> match) {
        String id = match.embeddingId();
        TextSegment segment = match.embedded();
        double score = match.score();

        return new ScoredSegment(id, segment, score, "VECTOR");
    }

    /**
     * ScoredSegment를 EmbeddingMatch로 변환
     *
     * @param scored ScoredSegment
     * @return EmbeddingMatch
     */
    public static EmbeddingMatch<TextSegment> toEmbeddingMatch(ScoredSegment scored) {
        return new EmbeddingMatch<>(
                scored.getScore(),
                scored.getId(),
                null, // embedding은 필요없음
                scored.getSegment()
        );
    }

    /**
     * Lucene Document에서 ID 추출
     *
     * @param doc Lucene Document
     * @return ID 문자열
     */
    public static String extractId(Document doc) {
        return doc.get("id");
    }

    /**
     * TextSegment에서 ID 추출 (메타데이터에서)
     *
     * @param segment TextSegment
     * @return ID 문자열
     */
    public static String extractId(TextSegment segment) {
        return segment.metadata().getString("id");
    }
}
