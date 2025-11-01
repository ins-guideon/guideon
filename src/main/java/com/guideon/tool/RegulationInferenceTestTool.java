package com.guideon.tool;

import com.guideon.config.RegulationInferenceConfigLoader;

import java.util.List;

/**
 * 규정 유형 추론 설정 테스트 도구
 *
 * regulation-inference-rules.yaml 설정을 로드하고
 * 다양한 쿼리에 대한 추론 결과를 테스트
 */
public class RegulationInferenceTestTool {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  Regulation Inference Configuration Test Tool        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        // 1. 설정 정보 출력
        System.out.println(">>> Step 1: Loading Configuration");
        System.out.println();
        RegulationInferenceConfigLoader.printConfig();

        // 2. 테스트 쿼리 목록
        String[] testQueries = {
            "경조사에 대한 규정을 알려줘",
            "경조휴가는 며칠인가요?",
            "경조금은 얼마나 받을 수 있나요?",
            "연차휴가 기준이 어떻게 되나요?",
            "해외출장비는 얼마인가요?",
            "급여는 언제 받나요?",
            "재택근무 규정이 있나요?",
            "퇴직금 계산 방법을 알려주세요",
            "건강검진은 언제 받나요?",
            "이사회는 언제 열리나요?",
            "계약서 검토는 어떻게 하나요?",
            "보안 규정을 알려주세요",
            "회계 처리는 어떻게 하나요?",
            "점심 메뉴 추천해줘"  // 매칭 안되는 쿼리
        };

        System.out.println();
        System.out.println(">>> Step 2: Testing Inference for Various Queries");
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════");

        for (String query : testQueries) {
            List<String> inferred = RegulationInferenceConfigLoader.inferRegulationTypes(query);

            System.out.println();
            System.out.println("Query: " + query);
            System.out.println("Inferred Types: " + inferred);

            if (inferred.size() == 1 && inferred.get(0).equals("일반")) {
                System.out.println("⚠️  No specific regulation matched (using default)");
            } else {
                System.out.println("✓  " + inferred.size() + " regulation type(s) inferred");
            }
            System.out.println("─────────────────────────────────────────────────────");
        }

        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  Test Summary                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        int totalQueries = testQueries.length;
        int matchedQueries = 0;

        for (String query : testQueries) {
            List<String> inferred = RegulationInferenceConfigLoader.inferRegulationTypes(query);
            if (!(inferred.size() == 1 && inferred.get(0).equals("일반"))) {
                matchedQueries++;
            }
        }

        int defaultQueries = totalQueries - matchedQueries;

        System.out.println("Total Test Queries: " + totalQueries);
        System.out.println("Matched Queries: " + matchedQueries + " (" +
            String.format("%.1f%%", (double)matchedQueries / totalQueries * 100) + ")");
        System.out.println("Default Queries: " + defaultQueries + " (" +
            String.format("%.1f%%", (double)defaultQueries / totalQueries * 100) + ")");
        System.out.println();

        if (matchedQueries > 0) {
            System.out.println("✅ Configuration is working correctly!");
        } else {
            System.out.println("⚠️  No queries matched - check configuration");
        }
        System.out.println();

        // 3. 특정 키워드별 테스트
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println(">>> Step 3: Keyword-specific Tests");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();

        testKeyword("경조사", "복리후생비규정");
        testKeyword("출장", "출장여비지급규정");
        testKeyword("급여", "급여규정");
        testKeyword("재택근무", "취업규칙");
        testKeyword("보안", "보안관리규정");

        System.out.println();
        System.out.println("✅ Regulation Inference Configuration Test Complete!");
        System.out.println();
    }

    /**
     * 특정 키워드가 예상된 규정 유형을 반환하는지 테스트
     */
    private static void testKeyword(String keyword, String expectedRegulation) {
        List<String> inferred = RegulationInferenceConfigLoader.inferRegulationTypes(keyword);

        boolean hasExpected = inferred.contains(expectedRegulation);

        System.out.printf("Keyword: %-15s → Expected: %-25s → ", keyword, expectedRegulation);
        if (hasExpected) {
            System.out.println("✓ PASS");
        } else {
            System.out.println("✗ FAIL (got: " + inferred + ")");
        }
    }
}
