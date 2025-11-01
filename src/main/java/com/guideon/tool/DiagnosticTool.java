package com.guideon.tool;

import com.guideon.analyzer.DictionaryLoader;
import com.guideon.analyzer.EnhancedKoreanAnalyzer;
import com.guideon.analyzer.SearchQueryAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 경조사 검색 문제 진단 도구
 *
 * 문제: "경조사에 대한 규정을 알려줘" 질문 시 복리후생비규정을 찾지 못함
 *
 * 진단 항목:
 * 1. 쿼리 토큰화 확인
 * 2. 문서 토큰화 확인
 * 3. 동의어 확장 확인
 * 4. 사용자 사전 적용 확인
 */
public class DiagnosticTool {
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticTool.class);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  경조사 검색 문제 진단 도구 (Diagnostic Tool)              ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            // 사전 로드
            System.out.println("📚 Loading dictionaries...");
            UserDictionary userDict = DictionaryLoader.loadUserDictionary();
            SynonymMap synonymMap = DictionaryLoader.loadSynonyms();

            System.out.println("✓ User dictionary: " + (userDict != null ? "Loaded" : "Not loaded"));
            System.out.println("✓ Synonym map: " + (synonymMap != null ? "Loaded" : "Not loaded"));
            System.out.println();

            // Analyzer 생성
            EnhancedKoreanAnalyzer indexAnalyzer = new EnhancedKoreanAnalyzer(
                userDict,
                DictionaryLoader.loadStopWords(),
                synonymMap,
                KoreanTokenizer.DecompoundMode.MIXED
            );

            SearchQueryAnalyzer queryAnalyzer = new SearchQueryAnalyzer(
                userDict,
                synonymMap,
                KoreanTokenizer.DecompoundMode.MIXED
            );

            // 테스트 케이스
            String[] queries = {
                "경조사에 대한 규정을 알려줘",
                "경조사",
                "경조사 규정",
                "경조휴가",
                "경조사휴가"
            };

            String[] documents = {
                "제21조(경조휴가) 1. 본인 결혼: 5일 2. 자녀 결혼: 2일",
                "경조사비 지원 규정",
                "복리후생비규정 경조금 지급 기준"
            };

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("📊 QUERY TOKENIZATION ANALYSIS");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();

            for (String query : queries) {
                List<String> tokens = analyze(queryAnalyzer, query);
                System.out.println("Query: " + query);
                System.out.println("Tokens: " + tokens);
                System.out.println("Token count: " + tokens.size());
                System.out.println("-----------------------------------------------------------");
            }

            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("📄 DOCUMENT TOKENIZATION ANALYSIS");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();

            for (String doc : documents) {
                List<String> tokens = analyze(indexAnalyzer, doc);
                System.out.println("Document: " + doc);
                System.out.println("Tokens: " + tokens);
                System.out.println("Token count: " + tokens.size());
                System.out.println("-----------------------------------------------------------");
            }

            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("🔍 MATCHING ANALYSIS");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();

            String testQuery = "경조사에 대한 규정을 알려줘";
            List<String> queryTokens = analyze(queryAnalyzer, testQuery);
            System.out.println("Test Query: " + testQuery);
            System.out.println("Query Tokens: " + queryTokens);
            System.out.println();

            for (String doc : documents) {
                List<String> docTokens = analyze(indexAnalyzer, doc);
                List<String> matches = findMatches(queryTokens, docTokens);

                System.out.println("Document: " + doc);
                System.out.println("Document Tokens: " + docTokens);
                System.out.println("Matching Tokens: " + matches);
                System.out.println("Match Score: " + matches.size() + "/" + queryTokens.size());

                if (matches.isEmpty()) {
                    System.out.println("⚠️ NO MATCHES - Document won't be retrieved!");
                } else {
                    System.out.println("✓ Has matches - Document may be retrieved");
                }
                System.out.println("-----------------------------------------------------------");
            }

            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║  DIAGNOSIS SUMMARY                                       ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println();

            // 진단 결과
            List<String> mainQueryTokens = analyze(queryAnalyzer, "경조사에 대한 규정을 알려줘");
            List<String> docTokens = analyze(indexAnalyzer, "제21조(경조휴가) 1. 본인 결혼: 5일");

            boolean hasMatch = findMatches(mainQueryTokens, docTokens).size() > 0;

            System.out.println("Query: \"경조사에 대한 규정을 알려줘\"");
            System.out.println("Query tokens: " + mainQueryTokens);
            System.out.println();
            System.out.println("Document: \"제21조(경조휴가) 1. 본인 결혼: 5일\"");
            System.out.println("Document tokens: " + docTokens);
            System.out.println();

            if (!hasMatch) {
                System.out.println("❌ PROBLEM IDENTIFIED:");
                System.out.println();

                // 문제 분석
                if (mainQueryTokens.contains("경조사")) {
                    if (docTokens.contains("경조휴가") || docTokens.contains("경조사휴가")) {
                        System.out.println("Issue: Synonym not working properly");
                        System.out.println("- Query has: 경조사");
                        System.out.println("- Document has: 경조휴가");
                        System.out.println("- Expected: Should match via synonym expansion");
                        System.out.println();
                        System.out.println("Solution: Check SynonymGraphFilter in analyzers");
                    } else {
                        System.out.println("Issue: Document doesn't contain expected tokens");
                    }
                } else {
                    System.out.println("Issue: Query tokenization removed '경조사'");
                    System.out.println("- This might be due to stopword filtering");
                    System.out.println();
                    System.out.println("Solution: Ensure '경조사' is not in stopwords");
                }
            } else {
                System.out.println("✅ NO ISSUES DETECTED");
                System.out.println("Query and document should match via BM25/Vector search");
            }

            System.out.println();
            System.out.println("✅ Diagnostic Complete!");
            System.out.println();

            // Cleanup
            indexAnalyzer.close();
            queryAnalyzer.close();

        } catch (Exception e) {
            logger.error("Error during diagnostic", e);
            e.printStackTrace();
        }
    }

    /**
     * Analyze text with given analyzer
     */
    private static List<String> analyze(Analyzer analyzer, String text) throws IOException {
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
     * Find matching tokens between two lists
     */
    private static List<String> findMatches(List<String> list1, List<String> list2) {
        List<String> matches = new ArrayList<>();
        for (String token : list1) {
            if (list2.contains(token) && !matches.contains(token)) {
                matches.add(token);
            }
        }
        return matches;
    }
}
