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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4.2 이후 신뢰도 하락 문제 분석 도구
 *
 * 문제:
 * - Phase 4.2 이전: 신뢰도 70-80%
 * - Phase 4.2 이후: 신뢰도 매우 낮음
 * - "경조사에 대한 규정을 알려줘" 검색 시 복리후생 규정 검색 안됨
 */
public class Phase42IssueAnalyzer {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  Phase 4.2 Issue Analyzer                               ║");
        System.out.println("║  신뢰도 하락 및 검색 실패 문제 분석                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        String testQuery = "경조사에 대한 규정을 알려줘";
        String testDoc = "제21조(경조휴가) 1. 본인 결혼: 5일 2. 자녀 결혼: 2일 3. 배우자, 본인 및 배우자의 부모 사망: 5일";

        try {
            // 사전 로드
            UserDictionary userDict = DictionaryLoader.loadUserDictionary();
            SynonymMap synonymMap = DictionaryLoader.loadSynonyms();

            System.out.println("📚 Dictionary Status:");
            System.out.println("  User Dictionary: " + (userDict != null ? "✓ Loaded" : "✗ Not loaded"));
            System.out.println("  Synonym Map: " + (synonymMap != null ? "✓ Loaded" : "✗ Not loaded"));
            System.out.println();

            // Phase 4.1 Analyzer (동의어 없음)
            EnhancedKoreanAnalyzer phase41Analyzer = new EnhancedKoreanAnalyzer(
                userDict,
                DictionaryLoader.loadStopWords(),
                null,  // 동의어 없음
                KoreanTokenizer.DecompoundMode.MIXED
            );

            // Phase 4.2 Analyzer (동의어 있음)
            EnhancedKoreanAnalyzer phase42Analyzer = new EnhancedKoreanAnalyzer(
                userDict,
                DictionaryLoader.loadStopWords(),
                synonymMap,  // 동의어 있음
                KoreanTokenizer.DecompoundMode.MIXED
            );

            // Search Query Analyzer (Phase 4.2)
            SearchQueryAnalyzer searchQueryAnalyzer = new SearchQueryAnalyzer(
                userDict,
                synonymMap,
                KoreanTokenizer.DecompoundMode.MIXED
            );

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("TEST 1: Query Tokenization Comparison");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();
            System.out.println("Query: \"" + testQuery + "\"");
            System.out.println();

            List<String> phase41QueryTokens = analyze(searchQueryAnalyzer, testQuery);
            System.out.println("Phase 4.1 (동의어 제외):");
            System.out.println("  Tokens: " + phase41QueryTokens);
            System.out.println("  Count: " + phase41QueryTokens.size());
            System.out.println();

            List<String> phase42QueryTokens = analyze(searchQueryAnalyzer, testQuery);
            System.out.println("Phase 4.2 (동의어 포함):");
            System.out.println("  Tokens: " + phase42QueryTokens);
            System.out.println("  Count: " + phase42QueryTokens.size());
            System.out.println();

            if (phase42QueryTokens.size() > phase41QueryTokens.size() * 2) {
                System.out.println("⚠️ WARNING: 토큰 수가 2배 이상 증가! (동의어 과다 확장 의심)");
            }
            System.out.println();

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("TEST 2: Document Tokenization Comparison");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();
            System.out.println("Document: \"" + testDoc + "\"");
            System.out.println();

            List<String> phase41DocTokens = analyze(phase41Analyzer, testDoc);
            System.out.println("Phase 4.1 (동의어 제외):");
            System.out.println("  Tokens: " + phase41DocTokens);
            System.out.println("  Count: " + phase41DocTokens.size());
            System.out.println();

            List<String> phase42DocTokens = analyze(phase42Analyzer, testDoc);
            System.out.println("Phase 4.2 (동의어 포함):");
            System.out.println("  Tokens: " + phase42DocTokens);
            System.out.println("  Count: " + phase42DocTokens.size());
            System.out.println();

            if (phase42DocTokens.size() > phase41DocTokens.size() * 3) {
                System.out.println("⚠️ WARNING: 문서 토큰 수가 3배 이상 증가! (인덱스 크기 폭증)");
            }
            System.out.println();

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("TEST 3: Token Matching Analysis");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();

            List<String> matches41 = findMatches(phase41QueryTokens, phase41DocTokens);
            List<String> matches42 = findMatches(phase42QueryTokens, phase42DocTokens);

            System.out.println("Phase 4.1 Matches:");
            System.out.println("  Matched Tokens: " + matches41);
            System.out.println("  Match Count: " + matches41.size() + "/" + phase41QueryTokens.size());
            double matchRate41 = (double) matches41.size() / phase41QueryTokens.size() * 100;
            System.out.println("  Match Rate: " + String.format("%.1f%%", matchRate41));
            System.out.println();

            System.out.println("Phase 4.2 Matches:");
            System.out.println("  Matched Tokens: " + matches42);
            System.out.println("  Match Count: " + matches42.size() + "/" + phase42QueryTokens.size());
            double matchRate42 = (double) matches42.size() / phase42QueryTokens.size() * 100;
            System.out.println("  Match Rate: " + String.format("%.1f%%", matchRate42));
            System.out.println();

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("DIAGNOSIS SUMMARY");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();

            boolean hasIssue = false;

            // 진단 1: 토큰 수 증가
            double tokenIncrease = (double) (phase42QueryTokens.size() - phase41QueryTokens.size())
                / phase41QueryTokens.size() * 100;
            System.out.println("1. Token Count Change:");
            System.out.println("   Query: " + phase41QueryTokens.size() + " → " + phase42QueryTokens.size()
                + " (" + (tokenIncrease >= 0 ? "+" : "") + String.format("%.0f%%", tokenIncrease) + ")");
            if (tokenIncrease > 100) {
                System.out.println("   ⚠️ ISSUE: 쿼리 토큰이 2배 이상 증가 (BM25 점수 희석 가능)");
                hasIssue = true;
            }
            System.out.println();

            // 진단 2: 매칭률 변화
            System.out.println("2. Match Rate Change:");
            System.out.println("   Phase 4.1: " + String.format("%.1f%%", matchRate41));
            System.out.println("   Phase 4.2: " + String.format("%.1f%%", matchRate42));
            double matchRateChange = matchRate42 - matchRate41;
            System.out.println("   Change: " + (matchRateChange >= 0 ? "+" : "")
                + String.format("%.1f%%", matchRateChange));
            if (matchRate42 < matchRate41) {
                System.out.println("   ⚠️ ISSUE: 매칭률이 감소함 (검색 정확도 하락)");
                hasIssue = true;
            }
            System.out.println();

            // 진단 3: 핵심 키워드 매칭
            System.out.println("3. Core Keyword Matching:");
            boolean has경조사 = phase42QueryTokens.contains("경조사");
            boolean has경조휴가 = phase42DocTokens.contains("경조휴가");
            System.out.println("   Query has '경조사': " + (has경조사 ? "✓" : "✗"));
            System.out.println("   Document has '경조휴가': " + (has경조휴가 ? "✓" : "✗"));

            boolean synonymMatched = false;
            if (has경조사 && has경조휴가) {
                // 동의어로 매칭되어야 함
                synonymMatched = matches42.contains("경조사") || matches42.contains("경조휴가")
                    || matches42.contains("경조사휴가");
                System.out.println("   Synonym Match: " + (synonymMatched ? "✓" : "✗"));
            }

            if (has경조사 && has경조휴가 && !synonymMatched) {
                System.out.println("   🚨 CRITICAL: '경조사' ↔ '경조휴가' 동의어 매칭 실패!");
                hasIssue = true;
            }
            System.out.println();

            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("RECOMMENDATIONS");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println();

            if (hasIssue) {
                System.out.println("⚠️ Issues detected! 다음 조치를 권장합니다:");
                System.out.println();

                if (tokenIncrease > 100) {
                    System.out.println("1. 동의어 과다 확장 문제:");
                    System.out.println("   - synonyms.txt에서 불필요한 동의어 제거");
                    System.out.println("   - 또는 SearchQueryAnalyzer에서만 동의어 사용");
                    System.out.println("   - EnhancedKoreanAnalyzer(인덱싱)에서는 동의어 제외 고려");
                    System.out.println();
                }

                if (matchRate42 < matchRate41) {
                    System.out.println("2. 매칭률 하락 문제:");
                    System.out.println("   - vector.search.min.score를 0.7 → 0.5로 복원");
                    System.out.println("   - 또는 BM25 가중치를 높임 (hybrid.search.keyword.weight 상향)");
                    System.out.println();
                }

                if (!synonymMatched && has경조사 && has경조휴가) {
                    System.out.println("3. 동의어 매칭 실패 문제:");
                    System.out.println("   - SynonymGraphFilter 설정 확인");
                    System.out.println("   - synonyms.txt 형식 검증");
                    System.out.println("   - BM25 인덱스 재구축 필요!");
                    System.out.println();
                }
            } else {
                System.out.println("✅ No major issues detected.");
                System.out.println("신뢰도 하락 원인은 다른 곳에 있을 수 있습니다:");
                System.out.println("  - Vector embedding 모델 변경?");
                System.out.println("  - 설정 파일 (application.properties) 변경?");
                System.out.println("  - 신뢰도 계산 로직 변경?");
            }

            System.out.println();
            System.out.println("✅ Analysis Complete!");
            System.out.println();

            // Cleanup
            phase41Analyzer.close();
            phase42Analyzer.close();
            searchQueryAnalyzer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
