package com.guideon.util;

import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.prompt.FewShotExample;
import com.guideon.model.prompt.IntentMetadata;
import com.guideon.util.prompt.FewShotExampleManager;
import com.guideon.util.prompt.PromptMetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * LLM 프롬프트 템플릿 관리 클래스
 * 질문 의도에 따라 최적화된 프롬프트를 생성
 */
public class PromptTemplate {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplate.class);
    
    // Few-shot 예제 사용 여부 (테스트용)
    private static boolean useFewShotExamples = true;
    
    // 메타데이터 사용 여부 (테스트용)
    private static boolean useMetadata = true;

    // 기본 시스템 프롬프트
    private static final String SYSTEM_PROMPT = """
        당신은 회사 규정 전문가입니다.
        직원들의 질문에 대해 정확하고 명확하게 답변해야 합니다.
        제공된 규정 내용만을 근거로 답변하며, 추측하거나 없는 내용을 만들어내지 마세요.
        """;

    // 공통 답변 작성 지침
    private static final String COMMON_GUIDELINES = """
        1. 간결하고 명확하게 답변하세요
        2. 전문 용어는 쉽게 풀어서 설명하세요
        3. 필요한 경우 예시를 들어 설명하세요
        4. 규정 조항을 인용할 때는 "취업규칙 제32조에 따르면..." 형식을 사용하세요
        5. 답변은 한국어로 자연스럽고 이해하기 쉽게 작성하세요
        """;

    /**
     * 의도별 최적화된 프롬프트 생성
     */
    public static String buildPrompt(String question, String structuredContext, QueryAnalysisResult analysis) {
        logger.debug("Building prompt for intent: {}", analysis != null ? analysis.getIntent() : "일반질문");

        String intent = (analysis != null && analysis.getIntent() != null) ? analysis.getIntent() : "일반질문";
        String intentGuidelines = getIntentSpecificGuidelines(intent);
        String examples = getIntentExamples(intent);

        String prompt = String.format("""
            %s

            [검색된 규정 내용]
            %s

            [질문]
            %s

            [답변 작성 지침]
            %s
            %s

            %s

            답변:
            """,
            SYSTEM_PROMPT.trim(),
            structuredContext,
            question,
            COMMON_GUIDELINES.trim(),
            intentGuidelines,
            examples);

        logger.info("Prompt built: length={} chars, intent={}", prompt.length(), intent);
        return prompt;
    }

    /**
     * 의도별 구체적인 가이드라인 (메타데이터 기반)
     */
    private static String getIntentSpecificGuidelines(String intent) {
        // 메타데이터 비활성화 시 공통 가이드라인만 반환
        if (!useMetadata) {
            return "";
        }
        
        // 메타데이터에서 가이드라인 가져오기
        IntentMetadata metadata = PromptMetadataManager.getIntentMetadata(intent);
        if (metadata != null && metadata.getGuidelines() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("\n\n[%s 답변 가이드]\n", intent));
            for (String guideline : metadata.getGuidelines()) {
                sb.append("- ").append(guideline).append("\n");
            }
            return sb.toString();
        }

        // 메타데이터가 없는 경우 기본 가이드라인 반환
        return switch (intent) {
            case "기준확인" -> """

                [기준확인 답변 가이드]
                - 구체적인 숫자, 금액, 기간 등을 명확히 제시하세요
                - 해당하는 규정 조항(제XX조)을 반드시 언급하세요
                - 조건이나 예외사항이 있다면 함께 설명하세요
                - 기준이 여러 경우로 나뉘면 명확히 구분해서 설명하세요
                - 가능한 경우 구체적인 예시를 들어 설명하세요 (예: "예를 들어, 7년 근속 시에는 18일...")
                - 답변은 충분히 상세하고 이해하기 쉽게 작성하세요
                """;

            case "절차설명" -> """

                [절차설명 답변 가이드]
                - 절차를 단계별로 순서대로 설명하세요 (1. 2. 3. 형식 사용)
                - 각 단계의 담당자나 부서를 명시하세요
                - 필요한 서류나 준비물이 있다면 언급하세요
                - 소요 기간이나 처리 시일이 있다면 안내하세요
                - 주의사항이나 유의할 점이 있다면 강조하세요
                - 각 단계를 구체적이고 상세하게 설명하여 사용자가 쉽게 따라할 수 있도록 하세요
                - 가능한 경우 실제 예시를 들어 설명하세요
                """;

            case "가능여부" -> """

                [가능여부 답변 가이드]
                - 첫 문장에서 "가능합니다" 또는 "불가능합니다"를 명확히 답변하세요
                - 그 근거가 되는 규정 조항을 제시하세요
                - 조건부로 가능한 경우, 그 조건을 명확히 설명하세요
                - 예외 상황이 있다면 함께 안내하세요
                - 가능한 경우 구체적인 예시를 들어 설명하세요
                - 답변은 충분히 상세하고 이해하기 쉽게 작성하세요
                """;

            case "예외상황" -> """

                [예외상황 답변 가이드]
                - 일반 원칙을 먼저 설명한 후 예외 사항을 설명하세요
                - 예외가 적용되는 조건을 구체적으로 명시하세요
                - 예외 승인이 필요한 경우 절차를 안내하세요
                - 유사하지만 다른 상황과 구별하여 설명하세요
                - 가능한 경우 구체적인 예시를 들어 예외 상황을 설명하세요
                - 답변은 충분히 상세하고 이해하기 쉽게 작성하세요
                """;

            case "계산방법" -> """

                [계산방법 답변 가이드]
                - 계산 공식이나 방법을 단계별로 설명하세요
                - 구체적인 예시를 들어 계산 과정을 보여주세요
                - 계산에 필요한 값들의 의미를 설명하세요
                - 반올림이나 소수점 처리 규칙이 있다면 명시하세요
                - 예시는 실제 숫자를 사용하여 구체적으로 보여주세요
                - 답변은 충분히 상세하고 이해하기 쉽게 작성하세요
                """;

            case "권리의무" -> """

                [권리의무 답변 가이드]
                - 권리와 의무를 명확히 구분하여 설명하세요
                - 권리를 행사하는 방법이나 절차를 안내하세요
                - 의무를 이행하지 않을 경우의 결과를 설명하세요
                - 관련 법령이나 상위 규정이 있다면 언급하세요
                - 가능한 경우 구체적인 예시를 들어 설명하세요
                - 답변은 충분히 상세하고 이해하기 쉽게 작성하세요
                """;

            default -> """

                [일반 답변 가이드]
                - 질문의 핵심에 초점을 맞춰 답변하세요
                - 관련된 모든 규정을 종합적으로 고려하여 답변하세요
                - 추가로 알아두면 유용한 정보가 있다면 함께 안내하세요
                - 가능한 경우 구체적인 예시를 들어 설명하세요
                - 답변은 충분히 상세하고 이해하기 쉽게 작성하세요
                """;
        };
    }

    /**
     * 의도별 답변 예시 (Few-shot 예제 기반)
     */
    private static String getIntentExamples(String intent) {
        // Few-shot 예제 사용 여부 확인
        if (!useFewShotExamples) {
            return "";
        }
        
        // Few-shot 예제 가져오기 (5-7개)
        List<FewShotExample> examples = FewShotExampleManager.getAnswerGenerationExamples(intent, 7);

        if (examples == null || examples.isEmpty()) {
            // 예제가 없는 경우 기본 예제 반환
            return getDefaultExamples(intent);
        }

        // Few-shot 예제를 프롬프트 형식으로 변환
        return FewShotExampleManager.formatAnswerGenerationExamples(examples);
    }
    
    /**
     * 테스트용: Few-shot 예제 사용 여부 설정
     */
    public static void setUseFewShotExamples(boolean useFewShotExamples) {
        PromptTemplate.useFewShotExamples = useFewShotExamples;
    }
    
    /**
     * Few-shot 예제 사용 여부 확인
     */
    public static boolean isUseFewShotExamples() {
        return useFewShotExamples;
    }
    
    /**
     * 테스트용: 메타데이터 사용 여부 설정
     */
    public static void setUseMetadata(boolean useMetadata) {
        PromptTemplate.useMetadata = useMetadata;
    }
    
    /**
     * 메타데이터 사용 여부 확인
     */
    public static boolean isUseMetadata() {
        return useMetadata;
    }

    /**
     * 기본 예제 반환 (Few-shot 예제가 없는 경우)
     */
    private static String getDefaultExamples(String intent) {
        return switch (intent) {
            case "기준확인" -> """
                [좋은 답변 예시]
                연차휴가 일수는 근속년수에 따라 다음과 같이 부여됩니다.

                취업규칙 제32조에 따르면:
                - 1년 근속: 15일
                - 3년 근속: 16일
                - 5년 근속: 17일
                - 이후 2년마다 1일씩 가산 (최대 25일)

                단, 입사 첫 해에는 월 단위로 계산하여 부여됩니다 (1개월 근무 시 1일).
                """;

            case "절차설명" -> """
                [좋은 답변 예시]
                연차휴가 신청 절차는 다음과 같습니다.

                1. 휴가 신청서 작성
                   - 사내 그룹웨어에서 "연차휴가 신청" 메뉴 접속
                   - 휴가 기간 및 사유 입력

                2. 결재 요청
                   - 직속 상사에게 전자결재 요청
                   - 통상 1~2일 내 승인 처리

                3. 승인 후 사용
                   - 승인 완료 시 자동으로 근태 시스템에 반영
                   - 긴급한 경우 구두 승인 후 사후 처리 가능

                📋 취업규칙 제15조 참조
                """;

            case "가능여부" -> """
                [좋은 답변 예시]
                네, 연차휴가를 시간 단위로 나누어 사용하는 것이 가능합니다.

                취업규칙 제16조에 따르면, 연차휴가는 다음과 같이 분할 사용할 수 있습니다:
                - 1일 단위 사용 (기본)
                - 반일(4시간) 단위 사용
                - 시간 단위 사용 (최소 1시간, 연간 최대 40시간)

                단, 시간 단위 사용 시에는 근무일 기준 3일 전까지 신청해야 하며,
                팀 업무 상황을 고려하여 상사의 승인이 필요합니다.
                """;

            default -> "";
        };
    }

    /**
     * 간단한 프롬프트 생성 (구조화된 컨텍스트 없이)
     */
    public static String buildSimplePrompt(String question, String context) {
        return String.format("""
            %s

            [규정 내용]
            %s

            [질문]
            %s

            [답변 지침]
            %s

            답변:
            """,
            SYSTEM_PROMPT.trim(),
            context,
            question,
            COMMON_GUIDELINES.trim());
    }

    /**
     * 후속 질문을 위한 프롬프트 생성
     */
    public static String buildFollowUpPrompt(String question, String previousAnswer, String context) {
        return String.format("""
            %s

            [이전 답변]
            %s

            [추가 규정 내용]
            %s

            [후속 질문]
            %s

            [답변 지침]
            - 이전 답변과 연관지어 설명하세요
            - 중복되는 내용은 간략히 언급하고 새로운 정보에 집중하세요
            - 이전 답변에서 언급하지 않은 추가 정보를 제공하세요

            답변:
            """,
            SYSTEM_PROMPT.trim(),
            previousAnswer,
            context,
            question);
    }
}
