package com.guideon.util.prompt;

import com.guideon.model.prompt.IntentMetadata;
import com.guideon.model.prompt.RegulationTypeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 프롬프트 메타데이터 관리 클래스
 * 규정 유형별, 질문 의도별 메타데이터 제공
 */
public class PromptMetadataManager {
    private static final Logger logger = LoggerFactory.getLogger(PromptMetadataManager.class);

    private static final Map<String, RegulationTypeMetadata> REGULATION_TYPE_METADATA = new HashMap<>();
    private static final Map<String, IntentMetadata> INTENT_METADATA = new HashMap<>();

    static {
        initializeRegulationTypeMetadata();
        initializeIntentMetadata();
        logger.info("Prompt metadata initialized: {} regulation types, {} intents",
                REGULATION_TYPE_METADATA.size(), INTENT_METADATA.size());
    }

    /**
     * 규정 유형별 메타데이터 초기화
     */
    private static void initializeRegulationTypeMetadata() {
        // 취업규칙
        REGULATION_TYPE_METADATA.put("취업규칙", new RegulationTypeMetadata(
                "취업규칙",
                "직원의 채용, 근무조건, 휴가, 급여 등 근로 관계 전반에 관한 규정",
                Arrays.asList("연차", "휴가", "근무", "급여", "수당", "야근", "재택근무", "채용", "퇴사", "승진"),
                Arrays.asList("기준확인", "절차설명", "가능여부", "권리의무", "정보조회"),
                "구체적인 숫자나 기준을 명시하고, 관련 조항(제XX조)을 반드시 언급하세요.",
                100
        ));

        // 복리후생비규정
        REGULATION_TYPE_METADATA.put("복리후생비규정", new RegulationTypeMetadata(
                "복리후생비규정",
                "직원의 복리후생, 경조사, 건강검진, 교육비 지원 등에 관한 규정",
                Arrays.asList("경조사", "경조휴가", "경조금", "건강검진", "교육비", "복리후생", "복지"),
                Arrays.asList("기준확인", "절차설명", "정보조회", "가능여부"),
                "경조사 유형별로 구분하여 설명하고, 금액이나 일수는 명확히 제시하세요.",
                95
        ));

        // 출장여비지급규정
        REGULATION_TYPE_METADATA.put("출장여비지급규정", new RegulationTypeMetadata(
                "출장여비지급규정",
                "출장 시 지급되는 교통비, 숙박비, 일비 등에 관한 규정",
                Arrays.asList("출장", "출장비", "여비", "교통비", "숙박비", "일비", "해외출장"),
                Arrays.asList("기준확인", "절차설명", "계산방법", "가능여부"),
                "지역별로 차등 지급되는 경우 명확히 구분하고, 정산 절차를 단계별로 설명하세요.",
                90
        ));

        // 경비지급규정
        REGULATION_TYPE_METADATA.put("경비지급규정", new RegulationTypeMetadata(
                "경비지급규정",
                "법인카드 사용, 경비 지급, 접대비 등에 관한 규정",
                Arrays.asList("경비", "법인카드", "접대비", "지출", "비용처리"),
                Arrays.asList("기준확인", "절차설명", "가능여부", "예외상황"),
                "직급별 한도를 명확히 제시하고, 사용 제한 사항을 강조하세요.",
                90
        ));

        // 임원퇴직금지급규정
        REGULATION_TYPE_METADATA.put("임원퇴직금지급규정", new RegulationTypeMetadata(
                "임원퇴직금지급규정",
                "임원의 퇴직금 계산 및 지급에 관한 규정",
                Arrays.asList("퇴직금", "퇴직수당", "퇴직연금", "퇴직"),
                Arrays.asList("계산방법", "기준확인", "절차설명"),
                "계산 공식을 단계별로 설명하고, 구체적인 예시를 들어 설명하세요.",
                85
        ));

        // 보안관리규정
        REGULATION_TYPE_METADATA.put("보안관리규정", new RegulationTypeMetadata(
                "보안관리규정",
                "정보 보안, 기밀 유지, 보안 관리에 관한 규정",
                Arrays.asList("보안", "정보보안", "기밀", "비밀유지", "개인정보"),
                Arrays.asList("권리의무", "정보조회", "예외상황"),
                "보안 위반 시의 결과를 명확히 설명하고, 의무 사항을 강조하세요.",
                85
        ));
    }

    /**
     * 질문 의도별 메타데이터 초기화
     */
    private static void initializeIntentMetadata() {
        // 기준확인
        INTENT_METADATA.put("기준확인", new IntentMetadata(
                "기준확인",
                "구체적인 숫자, 금액, 기간, 기준 등을 확인하는 질문",
                "구체적인 숫자, 금액, 기간 등을 명확히 제시하고, 해당하는 규정 조항(제XX조)을 반드시 언급하세요.",
                Arrays.asList(
                        "구체적인 숫자, 금액, 기간 등을 명확히 제시하세요",
                        "해당하는 규정 조항(제XX조)을 반드시 언급하세요",
                        "조건이나 예외사항이 있다면 함께 설명하세요",
                        "기준이 여러 경우로 나뉘면 명확히 구분해서 설명하세요"
                ),
                Arrays.asList("얼마", "몇", "언제", "어느", "기준", "한도", "일수", "금액"),
                Arrays.asList("취업규칙", "복리후생비규정", "출장여비지급규정", "경비지급규정")
        ));

        // 절차설명
        INTENT_METADATA.put("절차설명", new IntentMetadata(
                "절차설명",
                "신청, 정산, 승인 등의 절차를 묻는 질문",
                "절차를 단계별로 순서대로 설명하세요 (1. 2. 3. 형식 사용). 각 단계의 담당자나 부서를 명시하세요.",
                Arrays.asList(
                        "절차를 단계별로 순서대로 설명하세요 (1. 2. 3. 형식 사용)",
                        "각 단계의 담당자나 부서를 명시하세요",
                        "필요한 서류나 준비물이 있다면 언급하세요",
                        "소요 기간이나 처리 시일이 있다면 안내하세요",
                        "주의사항이나 유의할 점이 있다면 강조하세요"
                ),
                Arrays.asList("어떻게", "절차", "신청", "정산", "승인", "방법", "과정"),
                Arrays.asList("취업규칙", "복리후생비규정", "출장여비지급규정", "경비지급규정")
        ));

        // 가능여부
        INTENT_METADATA.put("가능여부", new IntentMetadata(
                "가능여부",
                "특정 행위나 조치가 가능한지 묻는 질문",
                "첫 문장에서 \"가능합니다\" 또는 \"불가능합니다\"를 명확히 답변하세요. 그 근거가 되는 규정 조항을 제시하세요.",
                Arrays.asList(
                        "첫 문장에서 \"가능합니다\" 또는 \"불가능합니다\"를 명확히 답변하세요",
                        "그 근거가 되는 규정 조항을 제시하세요",
                        "조건부로 가능한 경우, 그 조건을 명확히 설명하세요",
                        "예외 상황이 있다면 함께 안내하세요"
                ),
                Arrays.asList("가능", "불가능", "할 수", "사용할 수", "신청할 수", "받을 수"),
                Arrays.asList("취업규칙", "복리후생비규정", "출장여비지급규정", "경비지급규정")
        ));

        // 예외상황
        INTENT_METADATA.put("예외상황", new IntentMetadata(
                "예외상황",
                "일반 원칙의 예외나 특수한 경우를 묻는 질문",
                "일반 원칙을 먼저 설명한 후 예외 사항을 설명하세요. 예외가 적용되는 조건을 구체적으로 명시하세요.",
                Arrays.asList(
                        "일반 원칙을 먼저 설명한 후 예외 사항을 설명하세요",
                        "예외가 적용되는 조건을 구체적으로 명시하세요",
                        "예외 승인이 필요한 경우 절차를 안내하세요",
                        "유사하지만 다른 상황과 구별하여 설명하세요"
                ),
                Arrays.asList("예외", "특수", "제외", "제한", "불가", "경우"),
                Arrays.asList("취업규칙", "복리후생비규정", "출장여비지급규정", "경비지급규정")
        ));

        // 계산방법
        INTENT_METADATA.put("계산방법", new IntentMetadata(
                "계산방법",
                "금액이나 수치를 계산하는 방법을 묻는 질문",
                "계산 공식이나 방법을 단계별로 설명하세요. 구체적인 예시를 들어 계산 과정을 보여주세요.",
                Arrays.asList(
                        "계산 공식이나 방법을 단계별로 설명하세요",
                        "구체적인 예시를 들어 계산 과정을 보여주세요",
                        "계산에 필요한 값들의 의미를 설명하세요",
                        "반올림이나 소수점 처리 규칙이 있다면 명시하세요"
                ),
                Arrays.asList("계산", "산출", "구하기", "얼마", "공식", "방법"),
                Arrays.asList("취업규칙", "임원퇴직금지급규정", "출장여비지급규정")
        ));

        // 권리의무
        INTENT_METADATA.put("권리의무", new IntentMetadata(
                "권리의무",
                "직원이나 회사의 권리와 의무를 묻는 질문",
                "권리와 의무를 명확히 구분하여 설명하세요. 권리를 행사하는 방법이나 절차를 안내하세요.",
                Arrays.asList(
                        "권리와 의무를 명확히 구분하여 설명하세요",
                        "권리를 행사하는 방법이나 절차를 안내하세요",
                        "의무를 이행하지 않을 경우의 결과를 설명하세요",
                        "관련 법령이나 상위 규정이 있다면 언급하세요"
                ),
                Arrays.asList("권리", "의무", "할 수", "해야", "책임", "의무"),
                Arrays.asList("취업규칙", "보안관리규정")
        ));

        // 정보조회
        INTENT_METADATA.put("정보조회", new IntentMetadata(
                "정보조회",
                "규정이나 제도에 대한 일반적인 정보를 묻는 질문",
                "질문의 핵심에 초점을 맞춰 답변하세요. 관련된 모든 규정을 종합적으로 고려하여 답변하세요.",
                Arrays.asList(
                        "질문의 핵심에 초점을 맞춰 답변하세요",
                        "관련된 모든 규정을 종합적으로 고려하여 답변하세요",
                        "추가로 알아두면 유용한 정보가 있다면 함께 안내하세요",
                        "규정 조항을 인용할 때는 \"취업규칙 제32조에 따르면...\" 형식을 사용하세요"
                ),
                Arrays.asList("알려줘", "알려주세요", "무엇", "어떤", "규정", "제도"),
                Arrays.asList("취업규칙", "복리후생비규정", "출장여비지급규정", "경비지급규정", "보안관리규정")
        ));
    }

    /**
     * 규정 유형별 메타데이터 조회
     */
    public static RegulationTypeMetadata getRegulationTypeMetadata(String regulationType) {
        return REGULATION_TYPE_METADATA.getOrDefault(regulationType, null);
    }

    /**
     * 모든 규정 유형 메타데이터 조회
     */
    public static Map<String, RegulationTypeMetadata> getAllRegulationTypeMetadata() {
        return Collections.unmodifiableMap(REGULATION_TYPE_METADATA);
    }

    /**
     * 질문 의도별 메타데이터 조회
     */
    public static IntentMetadata getIntentMetadata(String intent) {
        return INTENT_METADATA.getOrDefault(intent, null);
    }

    /**
     * 모든 의도 메타데이터 조회
     */
    public static Map<String, IntentMetadata> getAllIntentMetadata() {
        return Collections.unmodifiableMap(INTENT_METADATA);
    }

    /**
     * 규정 유형에 대한 답변 형식 가이드 조회
     */
    public static String getAnswerFormatGuide(String regulationType) {
        RegulationTypeMetadata metadata = getRegulationTypeMetadata(regulationType);
        return metadata != null ? metadata.getAnswerFormatGuide() : "";
    }

    /**
     * 의도에 대한 답변 형식 가이드 조회
     */
    public static String getAnswerFormatGuideByIntent(String intent) {
        IntentMetadata metadata = getIntentMetadata(intent);
        return metadata != null ? metadata.getAnswerFormat() : "";
    }

    /**
     * 규정 유형 목록 조회 (우선순위 순)
     */
    public static List<String> getRegulationTypesByPriority() {
        return REGULATION_TYPE_METADATA.values().stream()
                .sorted(Comparator.comparing(RegulationTypeMetadata::getPriority).reversed())
                .map(RegulationTypeMetadata::getRegulationType)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 의도 목록 조회
     */
    public static List<String> getAllIntents() {
        return new ArrayList<>(INTENT_METADATA.keySet());
    }
}

