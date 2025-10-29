package com.guideon.tool;

import com.guideon.util.LuceneAnalyzerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4.1: Analyzer 테스트 도구
 *
 * 한국어 분석기의 토큰화 결과를 확인하고
 * Phase 3 기본 분석기와 Phase 4.1 향상된 분석기를 비교
 */
public class AnalyzerTestTool {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzerTestTool.class);

    /**
     * Analyzer로 텍스트를 분석하여 토큰 리스트 반환
     *
     * @param analyzer Lucene Analyzer
     * @param text 분석할 텍스트
     * @return 토큰 리스트
     */
    public static List<String> analyze(Analyzer analyzer, String text) throws IOException {
        List<String> tokens = new ArrayList<>();

        try (TokenStream stream = analyzer.tokenStream("content", text)) {
            CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
            stream.reset();

            while (stream.incrementToken()) {
                tokens.add(termAttr.toString());
            }

            stream.end();
        }

        return tokens;
    }

    /**
     * 분석 결과를 보기 좋게 출력
     */
    public static void printAnalysisResult(String label, String query, List<String> tokens) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📝 " + label);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Query: " + query);
        System.out.println("Tokens (" + tokens.size() + "): " + tokens);
        System.out.println();
    }

    /**
     * Before/After 비교 출력
     */
    public static void printComparison(String query, List<String> beforeTokens, List<String> afterTokens) {
        System.out.println("┌────────────────────────────────────────────────┐");
        System.out.println("│  Query: " + query);
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  BEFORE (Phase 3 - Basic)");
        System.out.println("│  Tokens: " + beforeTokens);
        System.out.println("│  Count: " + beforeTokens.size());
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  AFTER (Phase 4.1 - Enhanced)");
        System.out.println("│  Tokens: " + afterTokens);
        System.out.println("│  Count: " + afterTokens.size());
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  IMPROVEMENTS:");

        // 토큰 수 감소 (불용어 제거 효과)
        int reduction = beforeTokens.size() - afterTokens.size();
        if (reduction > 0) {
            System.out.println("│  ✅ Noise reduced: -" + reduction + " tokens");
        }

        // 새로 추가된 토큰 (사용자 사전 효과)
        List<String> newTokens = new ArrayList<>(afterTokens);
        newTokens.removeAll(beforeTokens);
        if (!newTokens.isEmpty()) {
            System.out.println("│  ✅ New tokens from user dict: " + newTokens);
        }

        // 제거된 토큰 (불용어)
        List<String> removedTokens = new ArrayList<>(beforeTokens);
        removedTokens.removeAll(afterTokens);
        if (!removedTokens.isEmpty()) {
            System.out.println("│  ✅ Removed stopwords: " + removedTokens);
        }

        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();
    }

    /**
     * 메인 테스트 실행
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║     Phase 4.1 Analyzer Test Tool                      ║");
        System.out.println("║     한국어 분석기 성능 비교 테스트                         ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        // 테스트 쿼리 목록
        String[] testQueries = {
            "해외출장비는 얼마인가요?",
            "연차휴가 기준이 어떻게 되나요?",
            "제32조에 대해 설명해주세요",
            "복리후생비규정을 확인하고 싶습니다",
            "반차는 어떻게 신청하나요?",
            "재택근무 규정이 있나요?",
            "시차출퇴근은 가능한가요?",
            "경조사휴가는 며칠인가요?",
            "야근수당은 얼마나 받을 수 있나요?",
            "퇴직금 계산 방법을 알려주세요"
        };

        try {
            // Analyzer 생성
            Analyzer basicAnalyzer = LuceneAnalyzerFactory.createBasicKoreanAnalyzer();
            Analyzer enhancedAnalyzer = LuceneAnalyzerFactory.createKoreanAnalyzer();

            System.out.println("📊 Test Summary:");
            System.out.println("  - Basic Analyzer: KoreanAnalyzer (Phase 3)");
            System.out.println("  - Enhanced Analyzer: EnhancedKoreanAnalyzer (Phase 4.1)");
            System.out.println("  - Test Queries: " + testQueries.length);
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println();

            int totalReduction = 0;
            int queriesWithImprovement = 0;

            for (String query : testQueries) {
                List<String> basicTokens = analyze(basicAnalyzer, query);
                List<String> enhancedTokens = analyze(enhancedAnalyzer, query);

                printComparison(query, basicTokens, enhancedTokens);

                int reduction = basicTokens.size() - enhancedTokens.size();
                totalReduction += reduction;
                if (reduction > 0) {
                    queriesWithImprovement++;
                }
            }

            // 최종 통계
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║                  FINAL STATISTICS                      ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("  Total queries tested: " + testQueries.length);
            System.out.println("  Queries with improvements: " + queriesWithImprovement);
            System.out.println("  Total token reduction: " + totalReduction);
            System.out.println("  Average reduction per query: " +
                String.format("%.1f", (double) totalReduction / testQueries.length));
            System.out.println();
            System.out.println("✅ Phase 4.1 Enhanced Analyzer Test Complete!");
            System.out.println();

            // Analyzer 닫기
            basicAnalyzer.close();
            enhancedAnalyzer.close();

        } catch (IOException e) {
            logger.error("Error during analyzer test", e);
            e.printStackTrace();
        }
    }
}
