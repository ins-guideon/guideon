package com.guideon.util.prompt;

import com.guideon.model.prompt.FewShotExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Few-shot 예제 관리 클래스
 * 질문 분석 및 답변 생성을 위한 예제 제공
 */
public class FewShotExampleManager {
    private static final Logger logger = LoggerFactory.getLogger(FewShotExampleManager.class);

    private static final List<FewShotExample> QUERY_ANALYSIS_EXAMPLES = new ArrayList<>();
    private static final Map<String, List<FewShotExample>> ANSWER_GENERATION_EXAMPLES = new HashMap<>();

    static {
        initializeQueryAnalysisExamples();
        initializeAnswerGenerationExamples();
        logger.info("Few-shot examples initialized: {} query analysis, {} intent types for answer generation",
                QUERY_ANALYSIS_EXAMPLES.size(), ANSWER_GENERATION_EXAMPLES.size());
    }

    /**
     * 질문 분석용 예제 초기화 (5-7개)
     */
    private static void initializeQueryAnalysisExamples() {
        // 예제 1: 경조사 관련
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-001",
                "경조사에 대한 규정을 알려줘",
                null,
                "KEYWORDS: 경조사, 경조휴가, 경조금\nREGULATION_TYPES: 복리후생비규정, 취업규칙\nINTENT: 정보조회\nSEARCH_QUERY: 경조사 경조휴가 경조금",
                "복리후생비규정",
                "정보조회",
                Arrays.asList("복리후생비규정", "취업규칙"),
                0.95,
                10,
                Arrays.asList("경조사", "복리후생"),
                "query_analysis"
        ));

        // 예제 2: 연차휴가 일수
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-002",
                "연차 휴가는 몇 일인가요?",
                null,
                "KEYWORDS: 연차, 휴가, 일수\nREGULATION_TYPES: 취업규칙\nINTENT: 기준확인\nSEARCH_QUERY: 연차 휴가 일수",
                "취업규칙",
                "기준확인",
                Arrays.asList("취업규칙"),
                0.92,
                15,
                Arrays.asList("연차", "휴가"),
                "query_analysis"
        ));

        // 예제 3: 출장비 신청 절차
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-003",
                "출장비 신청은 어떻게 하나요?",
                null,
                "KEYWORDS: 출장비, 신청, 절차\nREGULATION_TYPES: 출장여비지급규정\nINTENT: 절차설명\nSEARCH_QUERY: 출장비 신청 절차",
                "출장여비지급규정",
                "절차설명",
                Arrays.asList("출장여비지급규정"),
                0.90,
                12,
                Arrays.asList("출장", "절차"),
                "query_analysis"
        ));

        // 예제 4: 시간 단위 연차 사용 가능 여부
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-004",
                "연차를 시간 단위로 사용할 수 있나요?",
                null,
                "KEYWORDS: 연차, 시간 단위, 사용\nREGULATION_TYPES: 취업규칙\nINTENT: 가능여부\nSEARCH_QUERY: 연차 시간 단위 사용",
                "취업규칙",
                "가능여부",
                Arrays.asList("취업규칙"),
                0.88,
                8,
                Arrays.asList("연차", "시간단위"),
                "query_analysis"
        ));

        // 예제 5: 퇴직금 계산 방법
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-005",
                "퇴직금은 어떻게 계산하나요?",
                null,
                "KEYWORDS: 퇴직금, 계산\nREGULATION_TYPES: 임원퇴직금지급규정, 취업규칙\nINTENT: 계산방법\nSEARCH_QUERY: 퇴직금 계산",
                "임원퇴직금지급규정",
                "계산방법",
                Arrays.asList("임원퇴직금지급규정", "취업규칙"),
                0.93,
                9,
                Arrays.asList("퇴직금", "계산"),
                "query_analysis"
        ));

        // 예제 6: 경비 지급 예외 상황
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-006",
                "경비 지급에서 예외가 되는 경우가 있나요?",
                null,
                "KEYWORDS: 경비, 지급, 예외\nREGULATION_TYPES: 경비지급규정\nINTENT: 예외상황\nSEARCH_QUERY: 경비 지급 예외",
                "경비지급규정",
                "예외상황",
                Arrays.asList("경비지급규정"),
                0.87,
                6,
                Arrays.asList("경비", "예외"),
                "query_analysis"
        ));

        // 예제 7: 직원 권리와 의무
        QUERY_ANALYSIS_EXAMPLES.add(new FewShotExample(
                "qa-007",
                "직원의 권리와 의무는 무엇인가요?",
                null,
                "KEYWORDS: 직원, 권리, 의무\nREGULATION_TYPES: 취업규칙\nINTENT: 권리의무\nSEARCH_QUERY: 직원 권리 의무",
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.91,
                7,
                Arrays.asList("권리", "의무"),
                "query_analysis"
        ));
    }

    /**
     * 답변 생성용 예제 초기화 (의도별 5-7개)
     */
    private static void initializeAnswerGenerationExamples() {
        // 기준확인 예제 (5-7개)
        List<FewShotExample> 기준확인 = new ArrayList<>();
        기준확인.add(new FewShotExample(
                "ans-기준확인-001",
                "연차휴가 일수는 얼마인가요?",
                "연차휴가 일수는 근속년수에 따라 다음과 같이 부여됩니다.\n\n취업규칙 제32조에 따르면:\n- 1년 근속: 15일\n- 3년 근속: 16일\n- 5년 근속: 17일\n- 이후 2년마다 1일씩 가산 (최대 25일)\n\n단, 입사 첫 해에는 월 단위로 계산하여 부여됩니다 (1개월 근무 시 1일).",
                null,
                "취업규칙",
                "기준확인",
                Arrays.asList("취업규칙"),
                0.95,
                20,
                Arrays.asList("연차", "일수"),
                "answer_generation"
        ));
        기준확인.add(new FewShotExample(
                "ans-기준확인-002",
                "경조금은 얼마나 받을 수 있나요?",
                "경조금 지급 기준은 다음과 같습니다.\n\n복리후생비규정 제15조에 따르면:\n- 부모 사망: 50만원\n- 배우자 사망: 50만원\n- 자녀 사망: 30만원\n- 형제자매 사망: 20만원\n- 조부모 사망: 20만원\n\n단, 본인과의 관계를 증빙하는 서류가 필요합니다.",
                null,
                "복리후생비규정",
                "기준확인",
                Arrays.asList("복리후생비규정"),
                0.92,
                15,
                Arrays.asList("경조금", "금액"),
                "answer_generation"
        ));
        기준확인.add(new FewShotExample(
                "ans-기준확인-003",
                "출장 일비는 얼마인가요?",
                "출장 일비는 지역에 따라 차등 지급됩니다.\n\n출장여비지급규정 제8조에 따르면:\n- 수도권: 5만원\n- 광역시: 4만원\n- 기타 지역: 3만원\n\n해외 출장의 경우 국가별로 별도 기준이 적용됩니다.",
                null,
                "출장여비지급규정",
                "기준확인",
                Arrays.asList("출장여비지급규정"),
                0.90,
                12,
                Arrays.asList("출장", "일비"),
                "answer_generation"
        ));
        기준확인.add(new FewShotExample(
                "ans-기준확인-004",
                "야근 수당은 시간당 얼마인가요?",
                "야근 수당은 기본급의 150%를 지급합니다.\n\n취업규칙 제45조에 따르면:\n- 평일 야근: 시간당 기본급의 150%\n- 휴일 야근: 시간당 기본급의 200%\n- 심야 근무(22시 이후): 추가 50% 가산\n\n최소 지급 단위는 1시간입니다.",
                null,
                "취업규칙",
                "기준확인",
                Arrays.asList("취업규칙"),
                0.93,
                18,
                Arrays.asList("야근", "수당"),
                "answer_generation"
        ));
        기준확인.add(new FewShotExample(
                "ans-기준확인-005",
                "건강검진은 몇 년마다 받나요?",
                "건강검진은 매년 1회 실시됩니다.\n\n복리후생비규정 제22조에 따르면:\n- 정기 건강검진: 매년 1회 (회사 부담)\n- 대상: 전 직원\n- 검진 항목: 기본 검진 + 직종별 추가 검진\n\n40세 이상 직원은 추가로 암 검진을 받을 수 있습니다.",
                null,
                "복리후생비규정",
                "기준확인",
                Arrays.asList("복리후생비규정"),
                0.88,
                10,
                Arrays.asList("건강검진", "주기"),
                "answer_generation"
        ));
        기준확인.add(new FewShotExample(
                "ans-기준확인-006",
                "법인카드 한도는 얼마인가요?",
                "법인카드 한도는 직급과 용도에 따라 다릅니다.\n\n경비지급규정 제12조에 따르면:\n- 임원: 월 500만원\n- 부장: 월 300만원\n- 차장: 월 200만원\n- 과장: 월 100만원\n- 대리 이하: 월 50만원\n\n단, 긴급한 경우 사전 승인으로 한도 초과 사용 가능합니다.",
                null,
                "경비지급규정",
                "기준확인",
                Arrays.asList("경비지급규정"),
                0.91,
                14,
                Arrays.asList("법인카드", "한도"),
                "answer_generation"
        ));
        기준확인.add(new FewShotExample(
                "ans-기준확인-007",
                "교육비 지원 한도는 얼마인가요?",
                "교육비 지원 한도는 연간 200만원입니다.\n\n복리후생비규정 제28조에 따르면:\n- 자격증 취득: 연간 200만원\n- 학자금 지원: 연간 100만원\n- 외국어 교육: 연간 50만원\n\n단, 승인된 교육에 한해 지원되며, 합격 후 증빙서류 제출이 필요합니다.",
                null,
                "복리후생비규정",
                "기준확인",
                Arrays.asList("복리후생비규정"),
                0.89,
                11,
                Arrays.asList("교육비", "한도"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("기준확인", 기준확인);

        // 절차설명 예제 (5-7개)
        List<FewShotExample> 절차설명 = new ArrayList<>();
        절차설명.add(new FewShotExample(
                "ans-절차설명-001",
                "연차휴가 신청 절차는 어떻게 되나요?",
                "연차휴가 신청 절차는 다음과 같습니다.\n\n1. 휴가 신청서 작성\n   - 사내 그룹웨어에서 \"연차휴가 신청\" 메뉴 접속\n   - 휴가 기간 및 사유 입력\n\n2. 결재 요청\n   - 직속 상사에게 전자결재 요청\n   - 통상 1~2일 내 승인 처리\n\n3. 승인 후 사용\n   - 승인 완료 시 자동으로 근태 시스템에 반영\n   - 긴급한 경우 구두 승인 후 사후 처리 가능\n\n📋 취업규칙 제15조 참조",
                null,
                "취업규칙",
                "절차설명",
                Arrays.asList("취업규칙"),
                0.94,
                25,
                Arrays.asList("연차", "신청", "절차"),
                "answer_generation"
        ));
        절차설명.add(new FewShotExample(
                "ans-절차설명-002",
                "출장비 정산은 어떻게 하나요?",
                "출장비 정산 절차는 다음과 같습니다.\n\n1. 출장비 신청서 작성\n   - 출장 후 7일 이내에 그룹웨어에서 신청\n   - 교통비, 숙박비, 일비 등 항목별 입력\n\n2. 증빙서류 첨부\n   - 교통비: 승차권, 영수증\n   - 숙박비: 호텔 영수증\n   - 일비: 별도 증빙 불필요\n\n3. 결재 진행\n   - 부서장 결재 후 경리팀으로 전송\n   - 통상 3~5일 내 지급\n\n📋 출장여비지급규정 제10조 참조",
                null,
                "출장여비지급규정",
                "절차설명",
                Arrays.asList("출장여비지급규정"),
                0.92,
                20,
                Arrays.asList("출장비", "정산"),
                "answer_generation"
        ));
        절차설명.add(new FewShotExample(
                "ans-절차설명-003",
                "경조휴가 신청은 어떻게 하나요?",
                "경조휴가 신청 절차는 다음과 같습니다.\n\n1. 경조사 발생 시 즉시 신청\n   - 그룹웨어 \"경조휴가 신청\" 메뉴 접속\n   - 경조사 유형 선택 (부모 사망, 결혼 등)\n\n2. 증빙서류 제출\n   - 관계 증명서류 (가족관계증명서 등)\n   - 경조사 발생 증빙 (사망진단서, 혼인신고서 등)\n\n3. 승인 및 사용\n   - 인사팀 확인 후 승인\n   - 승인일로부터 30일 이내 사용\n\n📋 복리후생비규정 제16조 참조",
                null,
                "복리후생비규정",
                "절차설명",
                Arrays.asList("복리후생비규정"),
                0.90,
                15,
                Arrays.asList("경조휴가", "신청"),
                "answer_generation"
        ));
        절차설명.add(new FewShotExample(
                "ans-절차설명-004",
                "법인카드 발급 신청은 어떻게 하나요?",
                "법인카드 발급 신청 절차는 다음과 같습니다.\n\n1. 발급 신청서 작성\n   - 경리팀에 법인카드 발급 신청서 제출\n   - 발급 사유 및 사용 용도 명시\n\n2. 부서장 승인\n   - 직속 상사 및 부서장 결재\n   - 발급 필요성 검토\n\n3. 카드 발급 및 수령\n   - 경리팀에서 카드 발급 (약 1주일 소요)\n   - 카드 수령 후 사용 규정 숙지\n\n📋 경비지급규정 제11조 참조",
                null,
                "경비지급규정",
                "절차설명",
                Arrays.asList("경비지급규정"),
                0.88,
                12,
                Arrays.asList("법인카드", "발급"),
                "answer_generation"
        ));
        절차설명.add(new FewShotExample(
                "ans-절차설명-005",
                "교육비 지원 신청은 어떻게 하나요?",
                "교육비 지원 신청 절차는 다음과 같습니다.\n\n1. 교육 사전 승인\n   - 교육 신청 전 부서장 사전 승인\n   - 교육 목적 및 필요성 명시\n\n2. 교육 이수\n   - 승인된 교육 이수\n   - 수료증 또는 합격증 취득\n\n3. 교육비 정산\n   - 교육비 영수증 및 수료증 제출\n   - 그룹웨어에서 교육비 지원 신청\n   - 경리팀 확인 후 지급\n\n📋 복리후생비규정 제28조 참조",
                null,
                "복리후생비규정",
                "절차설명",
                Arrays.asList("복리후생비규정"),
                0.91,
                18,
                Arrays.asList("교육비", "지원"),
                "answer_generation"
        ));
        절차설명.add(new FewShotExample(
                "ans-절차설명-006",
                "건강검진 신청은 어떻게 하나요?",
                "건강검진 신청 절차는 다음과 같습니다.\n\n1. 검진 일정 확인\n   - 인사팀에서 연간 검진 일정 공지\n   - 개인별 검진 가능 기간 확인\n\n2. 검진 예약\n   - 지정 병원에 전화 또는 온라인 예약\n   - 검진 항목 선택 (기본 + 선택)\n\n3. 검진 실시 및 결과 확인\n   - 예약일 검진 실시\n   - 결과는 2주 후 온라인으로 확인 가능\n\n📋 복리후생비규정 제22조 참조",
                null,
                "복리후생비규정",
                "절차설명",
                Arrays.asList("복리후생비규정"),
                0.89,
                16,
                Arrays.asList("건강검진", "신청"),
                "answer_generation"
        ));
        절차설명.add(new FewShotExample(
                "ans-절차설명-007",
                "재택근무 신청은 어떻게 하나요?",
                "재택근무 신청 절차는 다음과 같습니다.\n\n1. 재택근무 신청서 작성\n   - 그룹웨어 \"재택근무 신청\" 메뉴 접속\n   - 재택근무 사유 및 기간 입력\n\n2. 부서장 승인\n   - 직속 상사 및 부서장 결재\n   - 업무 연속성 검토\n\n3. 승인 후 재택근무 실시\n   - 승인일부터 재택근무 시작\n   - 일일 업무 보고 필수\n\n📋 취업규칙 제25조 참조",
                null,
                "취업규칙",
                "절차설명",
                Arrays.asList("취업규칙"),
                0.87,
                13,
                Arrays.asList("재택근무", "신청"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("절차설명", 절차설명);

        // 가능여부 예제 (5-7개)
        List<FewShotExample> 가능여부 = new ArrayList<>();
        가능여부.add(new FewShotExample(
                "ans-가능여부-001",
                "연차를 시간 단위로 나누어 사용할 수 있나요?",
                "네, 연차휴가를 시간 단위로 나누어 사용하는 것이 가능합니다.\n\n취업규칙 제16조에 따르면, 연차휴가는 다음과 같이 분할 사용할 수 있습니다:\n- 1일 단위 사용 (기본)\n- 반일(4시간) 단위 사용\n- 시간 단위 사용 (최소 1시간, 연간 최대 40시간)\n\n단, 시간 단위 사용 시에는 근무일 기준 3일 전까지 신청해야 하며, 팀 업무 상황을 고려하여 상사의 승인이 필요합니다.",
                null,
                "취업규칙",
                "가능여부",
                Arrays.asList("취업규칙"),
                0.95,
                22,
                Arrays.asList("연차", "시간단위"),
                "answer_generation"
        ));
        가능여부.add(new FewShotExample(
                "ans-가능여부-002",
                "경조휴가를 연차로 대체할 수 있나요?",
                "아니요, 경조휴가는 별도로 부여되는 휴가이므로 연차로 대체할 수 없습니다.\n\n복리후생비규정 제16조에 따르면, 경조휴가는 경조사 발생 시 별도로 부여되는 유급휴가입니다. 연차휴가와는 별개로 관리되며, 미사용 시 소멸됩니다.\n\n단, 경조사 발생일이 휴일인 경우에는 평일로 대체하여 사용할 수 있습니다.",
                null,
                "복리후생비규정",
                "가능여부",
                Arrays.asList("복리후생비규정"),
                0.92,
                15,
                Arrays.asList("경조휴가", "연차"),
                "answer_generation"
        ));
        가능여부.add(new FewShotExample(
                "ans-가능여부-003",
                "출장비를 선지급받을 수 있나요?",
                "네, 출장비를 선지급받을 수 있습니다.\n\n출장여비지급규정 제9조에 따르면, 해외 출장 또는 장기 출장(3일 이상)의 경우 사전 신청을 통해 선지급이 가능합니다.\n\n선지급 신청 절차:\n1. 출장 승인 후 경리팀에 선지급 신청\n2. 예상 출장비 금액 산정\n3. 출장 후 실제 지출액과 정산\n\n단, 국내 단기 출장(2일 이하)은 사후 정산만 가능합니다.",
                null,
                "출장여비지급규정",
                "가능여부",
                Arrays.asList("출장여비지급규정"),
                0.90,
                18,
                Arrays.asList("출장비", "선지급"),
                "answer_generation"
        ));
        가능여부.add(new FewShotExample(
                "ans-가능여부-004",
                "법인카드를 개인 용도로 사용할 수 있나요?",
                "아니요, 법인카드는 업무 용도로만 사용해야 하며 개인 용도로 사용할 수 없습니다.\n\n경비지급규정 제13조에 따르면, 법인카드는 다음 용도로만 사용 가능합니다:\n- 업무 관련 식대 및 접대비\n- 업무 관련 교통비\n- 업무 관련 구매비용\n\n개인 용도로 사용한 경우 즉시 반납하고, 사용 금액을 변상해야 합니다. 반복 위반 시 카드 사용 권한이 박탈될 수 있습니다.",
                null,
                "경비지급규정",
                "가능여부",
                Arrays.asList("경비지급규정"),
                0.94,
                20,
                Arrays.asList("법인카드", "개인용도"),
                "answer_generation"
        ));
        가능여부.add(new FewShotExample(
                "ans-가능여부-005",
                "연차를 다음 해로 이월할 수 있나요?",
                "네, 일부 연차는 다음 해로 이월할 수 있습니다.\n\n취업규칙 제33조에 따르면:\n- 연차의 50%까지 다음 해 3월 말까지 이월 가능\n- 나머지 50%는 당해 연도 내 사용해야 하며 미사용 시 소멸\n\n예를 들어, 20일의 연차가 있다면:\n- 10일은 당해 연도 내 사용\n- 10일은 다음 해 3월 말까지 이월 가능\n\n단, 이월된 연차도 기한 내 미사용 시 소멸됩니다.",
                null,
                "취업규칙",
                "가능여부",
                Arrays.asList("취업규칙"),
                0.91,
                17,
                Arrays.asList("연차", "이월"),
                "answer_generation"
        ));
        가능여부.add(new FewShotExample(
                "ans-가능여부-006",
                "교육비 지원을 받고 퇴사할 수 있나요?",
                "조건부로 가능합니다.\n\n복리후생비규정 제29조에 따르면:\n- 교육비 지원 후 1년 이내 퇴사 시: 지원 금액의 50% 반환\n- 교육비 지원 후 2년 이내 퇴사 시: 지원 금액의 30% 반환\n- 교육비 지원 후 2년 이상 근무 후 퇴사: 반환 불필요\n\n단, 회사 사정으로 인한 퇴사(정리해고 등)의 경우 반환 의무가 없습니다.",
                null,
                "복리후생비규정",
                "가능여부",
                Arrays.asList("복리후생비규정"),
                0.88,
                14,
                Arrays.asList("교육비", "퇴사"),
                "answer_generation"
        ));
        가능여부.add(new FewShotExample(
                "ans-가능여부-007",
                "재택근무 중에도 출근 수당을 받을 수 있나요?",
                "아니요, 재택근무 중에는 출근 수당을 받을 수 없습니다.\n\n취업규칙 제26조에 따르면, 출근 수당은 실제 사무실 출근 시에만 지급됩니다. 재택근무는 출근으로 간주되지 않으므로 출근 수당이 지급되지 않습니다.\n\n단, 재택근무 중에도 정규 근무 시간을 준수해야 하며, 일일 업무 보고가 필수입니다.",
                null,
                "취업규칙",
                "가능여부",
                Arrays.asList("취업규칙"),
                0.86,
                11,
                Arrays.asList("재택근무", "출근수당"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("가능여부", 가능여부);

        // 예외상황 예제 (5-7개)
        List<FewShotExample> 예외상황 = new ArrayList<>();
        예외상황.add(new FewShotExample(
                "ans-예외상황-001",
                "경비 지급에서 예외가 되는 경우가 있나요?",
                "네, 경비 지급에서 예외가 되는 경우가 있습니다.\n\n경비지급규정 제7조에 따르면, 다음의 경우 경비 지급이 제한되거나 불가능합니다:\n\n1. 증빙서류 미제출\n   - 영수증이나 증빙서류가 없는 경우 지급 불가\n   - 단, 일비는 증빙 불필요\n\n2. 개인 용도 지출\n   - 개인 용도로 사용한 비용은 지급 불가\n   - 업무와 무관한 지출은 제외\n\n3. 한도 초과\n   - 직급별 한도를 초과한 경우 초과분은 지급 불가\n   - 단, 사전 승인 시 예외 가능",
                null,
                "경비지급규정",
                "예외상황",
                Arrays.asList("경비지급규정"),
                0.93,
                16,
                Arrays.asList("경비", "예외"),
                "answer_generation"
        ));
        예외상황.add(new FewShotExample(
                "ans-예외상황-002",
                "연차 사용에 예외가 있나요?",
                "네, 연차 사용에 예외가 있습니다.\n\n취업규칙 제17조에 따르면:\n\n1. 업무 긴급 상황\n   - 중요한 프로젝트 진행 중에는 사용 제한 가능\n   - 단, 부서장 사전 승인 필요\n\n2. 동시 사용 제한\n   - 같은 부서의 30% 이상이 동시 사용 불가\n   - 선착순 또는 업무 상황 고려하여 조정\n\n3. 특정 기간 제한\n   - 결산 기간, 중요한 회의 기간 등\n   - 사전 공지 후 사용 제한 가능",
                null,
                "취업규칙",
                "예외상황",
                Arrays.asList("취업규칙"),
                0.91,
                19,
                Arrays.asList("연차", "예외"),
                "answer_generation"
        ));
        예외상황.add(new FewShotExample(
                "ans-예외상황-003",
                "출장비 정산에 예외가 있나요?",
                "네, 출장비 정산에 예외가 있습니다.\n\n출장여비지급규정 제11조에 따르면:\n\n1. 정산 기한 초과\n   - 출장 후 30일 이내 정산하지 않으면 지급 불가\n   - 단, 부득이한 사유 시 경리팀에 사전 협의 가능\n\n2. 증빙서류 부실\n   - 영수증이 불명확하거나 위조된 경우 지급 불가\n   - 재발행 불가능한 경우 별도 확인 절차 필요\n\n3. 불필요한 지출\n   - 업무와 무관한 지출은 제외\n   - 사치성 지출은 지급 불가",
                null,
                "출장여비지급규정",
                "예외상황",
                Arrays.asList("출장여비지급규정"),
                0.89,
                14,
                Arrays.asList("출장비", "정산", "예외"),
                "answer_generation"
        ));
        예외상황.add(new FewShotExample(
                "ans-예외상황-004",
                "경조휴가 사용에 예외가 있나요?",
                "네, 경조휴가 사용에 예외가 있습니다.\n\n복리후생비규정 제17조에 따르면:\n\n1. 경조사 발생일이 휴일인 경우\n   - 평일로 대체하여 사용 가능\n   - 단, 경조사 발생일로부터 30일 이내 사용\n\n2. 중복 경조사\n   - 같은 기간에 여러 경조사 발생 시 가장 긴 휴가일수 적용\n   - 중복 지급 불가\n\n3. 증빙서류 미제출\n   - 관계 증명서류를 제출하지 않으면 휴가 사용 불가\n   - 사후 제출 시에도 휴가 취소 가능",
                null,
                "복리후생비규정",
                "예외상황",
                Arrays.asList("복리후생비규정"),
                0.87,
                12,
                Arrays.asList("경조휴가", "예외"),
                "answer_generation"
        ));
        예외상황.add(new FewShotExample(
                "ans-예외상황-005",
                "법인카드 사용에 예외가 있나요?",
                "네, 법인카드 사용에 예외가 있습니다.\n\n경비지급규정 제14조에 따르면:\n\n1. 한도 초과 사용\n   - 월 한도를 초과한 경우 추가 사용 불가\n   - 단, 긴급한 경우 사전 승인으로 초과 사용 가능\n\n2. 사용 제한 업종\n   - 유흥업소, 도박 관련 업소 사용 불가\n   - 개인 용도 사용 불가\n\n3. 미승인 사용\n   - 부서장 사전 승인 없이 사용한 경우\n   - 사용 금액 변상 및 카드 회수 가능",
                null,
                "경비지급규정",
                "예외상황",
                Arrays.asList("경비지급규정"),
                0.92,
                17,
                Arrays.asList("법인카드", "예외"),
                "answer_generation"
        ));
        예외상황.add(new FewShotExample(
                "ans-예외상황-006",
                "교육비 지원에 예외가 있나요?",
                "네, 교육비 지원에 예외가 있습니다.\n\n복리후생비규정 제30조에 따르면:\n\n1. 미승인 교육\n   - 사전 승인 없이 수강한 교육은 지원 불가\n   - 업무와 무관한 교육은 제외\n\n2. 불합격\n   - 시험에 불합격한 경우 지원 불가\n   - 단, 수료증이 있는 경우 일부 지원 가능\n\n3. 중복 지원\n   - 같은 교육에 대해 중복 지원 불가\n   - 타 지원금과 중복 수령 불가",
                null,
                "복리후생비규정",
                "예외상황",
                Arrays.asList("복리후생비규정"),
                0.88,
                13,
                Arrays.asList("교육비", "예외"),
                "answer_generation"
        ));
        예외상황.add(new FewShotExample(
                "ans-예외상황-007",
                "건강검진에 예외가 있나요?",
                "네, 건강검진에 예외가 있습니다.\n\n복리후생비규정 제23조에 따르면:\n\n1. 검진 기간 외\n   - 지정된 검진 기간 외에는 검진비 지원 불가\n   - 단, 부득이한 사유 시 인사팀에 사전 협의\n\n2. 지정 병원 외\n   - 지정 병원에서 검진하지 않은 경우 지원 불가\n   - 단, 지역 특성상 지정 병원이 없는 경우 예외 가능\n\n3. 중복 검진\n   - 같은 연도에 2회 이상 검진한 경우 1회만 지원\n   - 개인 부담으로 추가 검진 가능",
                null,
                "복리후생비규정",
                "예외상황",
                Arrays.asList("복리후생비규정"),
                0.85,
                10,
                Arrays.asList("건강검진", "예외"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("예외상황", 예외상황);

        // 계산방법 예제 (5-7개)
        List<FewShotExample> 계산방법 = new ArrayList<>();
        계산방법.add(new FewShotExample(
                "ans-계산방법-001",
                "퇴직금은 어떻게 계산하나요?",
                "퇴직금은 다음과 같이 계산합니다.\n\n임원퇴직금지급규정 제5조에 따르면:\n\n계산 공식:\n퇴직금 = 평균임금 × 근속년수\n\n1. 평균임금 계산\n   - 퇴직일 이전 3개월간의 임금 총액 ÷ 3개월\n   - 기본급 + 각종 수당 포함\n\n2. 근속년수 계산\n   - 입사일부터 퇴직일까지의 기간\n   - 1년 미만은 월 단위로 계산 (1개월 = 1/12년)\n\n예시:\n- 평균임금: 300만원\n- 근속년수: 5년\n- 퇴직금: 300만원 × 5년 = 1,500만원",
                null,
                "임원퇴직금지급규정",
                "계산방법",
                Arrays.asList("임원퇴직금지급규정"),
                0.95,
                21,
                Arrays.asList("퇴직금", "계산"),
                "answer_generation"
        ));
        계산방법.add(new FewShotExample(
                "ans-계산방법-002",
                "야근 수당은 어떻게 계산하나요?",
                "야근 수당은 다음과 같이 계산합니다.\n\n취업규칙 제45조에 따르면:\n\n계산 공식:\n야근 수당 = 시간당 기본급 × 야근 시간 × 가산률\n\n1. 시간당 기본급 계산\n   - 월 기본급 ÷ 월 소정근로시간\n   - 예: 300만원 ÷ 209시간 = 14,354원/시간\n\n2. 가산률\n   - 평일 야근: 150%\n   - 휴일 야근: 200%\n   - 심야 근무(22시 이후): 추가 50% 가산\n\n예시:\n- 시간당 기본급: 14,354원\n- 평일 야근 2시간\n- 야근 수당: 14,354원 × 2시간 × 1.5 = 43,062원",
                null,
                "취업규칙",
                "계산방법",
                Arrays.asList("취업규칙"),
                0.93,
                19,
                Arrays.asList("야근", "수당", "계산"),
                "answer_generation"
        ));
        계산방법.add(new FewShotExample(
                "ans-계산방법-003",
                "연차 일수는 어떻게 계산하나요?",
                "연차 일수는 다음과 같이 계산합니다.\n\n취업규칙 제32조에 따르면:\n\n1. 기본 연차\n   - 1년 근속: 15일\n   - 3년 근속: 16일\n   - 5년 근속: 17일\n   - 이후 2년마다 1일씩 가산 (최대 25일)\n\n2. 입사 첫 해 계산\n   - 월 단위로 계산\n   - 1개월 근무 시 1일 부여\n   - 예: 6개월 근무 시 6일 부여\n\n3. 근속년수 계산\n   - 입사일 기준으로 매년 갱신\n   - 예: 2020년 1월 입사 → 2021년 1월부터 16일 부여",
                null,
                "취업규칙",
                "계산방법",
                Arrays.asList("취업규칙"),
                0.91,
                23,
                Arrays.asList("연차", "일수", "계산"),
                "answer_generation"
        ));
        계산방법.add(new FewShotExample(
                "ans-계산방법-004",
                "출장비는 어떻게 계산하나요?",
                "출장비는 다음과 같이 계산합니다.\n\n출장여비지급규정 제6조에 따르면:\n\n출장비 = 교통비 + 숙박비 + 일비\n\n1. 교통비\n   - 실제 지출액 (영수증 기준)\n   - 최소한의 교통수단 이용 원칙\n\n2. 숙박비\n   - 지역별 한도 내에서 지급\n   - 수도권: 10만원, 광역시: 8만원, 기타: 6만원\n\n3. 일비\n   - 지역별 차등 지급\n   - 수도권: 5만원, 광역시: 4만원, 기타: 3만원\n\n예시:\n- 수도권 출장 1일\n- 교통비: 5만원, 숙박비: 10만원, 일비: 5만원\n- 총 출장비: 20만원",
                null,
                "출장여비지급규정",
                "계산방법",
                Arrays.asList("출장여비지급규정"),
                0.89,
                17,
                Arrays.asList("출장비", "계산"),
                "answer_generation"
        ));
        계산방법.add(new FewShotExample(
                "ans-계산방법-005",
                "상여금은 어떻게 계산하나요?",
                "상여금은 다음과 같이 계산합니다.\n\n취업규칙 제40조에 따르면:\n\n계산 공식:\n상여금 = 기본급 × 상여율 × 성과지수\n\n1. 기본급\n   - 상여 지급 기준일의 기본급\n\n2. 상여율\n   - 연 2회 지급 (상반기, 하반기)\n   - 각 100% (기본급의 2배)\n\n3. 성과지수\n   - 개인 성과 평가 결과 반영\n   - 0.8 ~ 1.2 범위\n\n예시:\n- 기본급: 300만원\n- 상여율: 100%\n- 성과지수: 1.0\n- 상여금: 300만원 × 1.0 × 1.0 = 300만원",
                null,
                "취업규칙",
                "계산방법",
                Arrays.asList("취업규칙"),
                0.87,
                15,
                Arrays.asList("상여금", "계산"),
                "answer_generation"
        ));
        계산방법.add(new FewShotExample(
                "ans-계산방법-006",
                "연장근로 수당은 어떻게 계산하나요?",
                "연장근로 수당은 다음과 같이 계산합니다.\n\n취업규칙 제44조에 따르면:\n\n계산 공식:\n연장근로 수당 = 시간당 기본급 × 연장근로 시간 × 가산률\n\n1. 시간당 기본급\n   - 월 기본급 ÷ 월 소정근로시간\n\n2. 가산률\n   - 주 40시간 초과: 150%\n   - 휴일 연장근로: 200%\n\n3. 최소 지급 단위\n   - 30분 단위로 계산\n   - 30분 미만은 반올림\n\n예시:\n- 시간당 기본급: 14,354원\n- 주 40시간 초과 3시간\n- 연장근로 수당: 14,354원 × 3시간 × 1.5 = 64,593원",
                null,
                "취업규칙",
                "계산방법",
                Arrays.asList("취업규칙"),
                0.92,
                18,
                Arrays.asList("연장근로", "수당", "계산"),
                "answer_generation"
        ));
        계산방법.add(new FewShotExample(
                "ans-계산방법-007",
                "경조금은 어떻게 계산하나요?",
                "경조금은 경조사 유형에 따라 고정 금액으로 지급됩니다.\n\n복리후생비규정 제15조에 따르면:\n\n경조금 지급 기준:\n- 부모 사망: 50만원\n- 배우자 사망: 50만원\n- 자녀 사망: 30만원\n- 형제자매 사망: 20만원\n- 조부모 사망: 20만원\n\n계산 방법:\n- 경조사 유형에 따라 정해진 금액 지급\n- 추가 계산 없이 고정 금액\n- 중복 경조사 발생 시 가장 높은 금액 적용\n\n예시:\n- 부모 사망 시: 50만원\n- 형제자매 사망 시: 20만원",
                null,
                "복리후생비규정",
                "계산방법",
                Arrays.asList("복리후생비규정"),
                0.90,
                16,
                Arrays.asList("경조금", "계산"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("계산방법", 계산방법);

        // 권리의무 예제 (5-7개)
        List<FewShotExample> 권리의무 = new ArrayList<>();
        권리의무.add(new FewShotExample(
                "ans-권리의무-001",
                "직원의 권리와 의무는 무엇인가요?",
                "직원의 권리와 의무는 다음과 같습니다.\n\n취업규칙 제3조에 따르면:\n\n【직원의 권리】\n1. 정당한 임금 수령권\n   - 근로 계약서에 명시된 임금 수령\n   - 각종 수당 및 상여금 수령\n\n2. 휴가 사용권\n   - 연차휴가, 경조휴가 등 휴가 사용\n   - 법정 휴일 휴무\n\n3. 근로 환경 보호권\n   - 안전하고 쾌적한 근로 환경\n   - 건강검진 등 복리후생 혜택\n\n【직원의 의무】\n1. 성실 근무 의무\n   - 업무 성실 이행\n   - 지시 사항 준수\n\n2. 비밀 유지 의무\n   - 업무상 알게 된 기밀 정보 보호\n   - 퇴사 후에도 유지\n\n3. 회사 재산 보호 의무\n   - 회사 자산의 보호 및 관리\n   - 불법 사용 금지",
                null,
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.94,
                24,
                Arrays.asList("권리", "의무"),
                "answer_generation"
        ));
        권리의무.add(new FewShotExample(
                "ans-권리의무-002",
                "회사의 권리와 의무는 무엇인가요?",
                "회사의 권리와 의무는 다음과 같습니다.\n\n취업규칙 제4조에 따르면:\n\n【회사의 권리】\n1. 인사 관리권\n   - 직원 배치 및 전보\n   - 승진 및 인사 평가\n\n2. 업무 지시권\n   - 업무 지시 및 감독\n   - 업무 규정 제정\n\n3. 징계권\n   - 규정 위반 시 징계\n   - 해고 등 인사 조치\n\n【회사의 의무】\n1. 임금 지급 의무\n   - 정당한 임금 지급\n   - 각종 수당 지급\n\n2. 근로 환경 제공 의무\n   - 안전한 근로 환경 조성\n   - 복리후생 제공\n\n3. 교육 기회 제공 의무\n   - 직무 교육 제공\n   - 승진 기회 제공",
                null,
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.92,
                20,
                Arrays.asList("회사", "권리", "의무"),
                "answer_generation"
        ));
        권리의무.add(new FewShotExample(
                "ans-권리의무-003",
                "직원이 휴가를 거부당할 수 있나요?",
                "일반적으로는 불가능하지만, 특정 조건에서는 가능합니다.\n\n취업규칙 제18조에 따르면:\n\n【직원의 권리】\n- 정당한 사유로 신청한 휴가는 거부될 수 없음\n- 사전 신청한 휴가는 승인되어야 함\n\n【예외 상황】\n1. 업무 긴급 상황\n   - 중요한 프로젝트 진행 중\n   - 단, 부서장이 사전에 공지한 경우\n\n2. 동시 사용 제한\n   - 같은 부서의 30% 이상 동시 사용 시\n   - 선착순 또는 업무 상황 고려\n\n3. 특정 기간 제한\n   - 결산 기간 등 중요한 기간\n   - 사전 공지 후 제한 가능",
                null,
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.90,
                18,
                Arrays.asList("휴가", "거부"),
                "answer_generation"
        ));
        권리의무.add(new FewShotExample(
                "ans-권리의무-004",
                "회사가 임금을 지급하지 않으면 어떻게 하나요?",
                "임금 미지급 시 다음과 같이 대응할 수 있습니다.\n\n취업규칙 제38조에 따르면:\n\n【직원의 권리】\n1. 임금 청구권\n   - 정당한 임금 수령 권리\n   - 지급일로부터 3년간 청구 가능\n\n2. 행정 구제\n   - 고용노동부에 신고\n   - 임금 체불 신고 가능\n\n3. 법적 구제\n   - 노동위원회에 진정 제기\n   - 법원에 소송 제기 가능\n\n【회사의 의무】\n- 정당한 사유 없이 임금 지급 거부 불가\n- 지급일을 준수해야 함\n- 임금 명세서 제공 의무",
                null,
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.88,
                15,
                Arrays.asList("임금", "미지급"),
                "answer_generation"
        ));
        권리의무.add(new FewShotExample(
                "ans-권리의무-005",
                "직원이 회사 정보를 외부에 공개할 수 있나요?",
                "아니요, 직원은 회사 정보를 외부에 공개할 수 없습니다.\n\n보안관리규정 제10조에 따르면:\n\n【직원의 의무】\n1. 기밀 정보 보호 의무\n   - 업무상 알게 된 기밀 정보 보호\n   - 외부 유출 금지\n\n2. 비밀 유지 의무\n   - 재직 중뿐만 아니라 퇴사 후에도 유지\n   - 계약 위반 시 손해배상 책임\n\n3. 정보 보안 준수\n   - 정보 보안 규정 준수\n   - 외부 유출 시 징계 대상\n\n【예외】\n- 법원의 영장이나 수사기관의 요청\n- 공개 의무가 있는 법적 요구",
                null,
                "보안관리규정",
                "권리의무",
                Arrays.asList("보안관리규정"),
                0.91,
                17,
                Arrays.asList("정보", "공개"),
                "answer_generation"
        ));
        권리의무.add(new FewShotExample(
                "ans-권리의무-006",
                "회사가 직원을 해고할 수 있는 경우는?",
                "회사는 정당한 사유가 있는 경우에만 해고할 수 있습니다.\n\n취업규칙 제50조에 따르면:\n\n【정당한 해고 사유】\n1. 근로 계약 위반\n   - 고의적 업무 태만\n   - 무단 결근\n\n2. 회사 규정 위반\n   - 중대한 규정 위반\n   - 반복적 경고 무시\n\n3. 부정행위\n   - 회사 자산 유용\n   - 부정한 행위\n\n【해고 절차】\n- 사전 경고 및 기회 부여\n- 징계 절차 준수\n- 해고 사유 명시\n\n단, 정당한 사유 없이 해고한 경우 부당해고로 인정될 수 있습니다.",
                null,
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.89,
                16,
                Arrays.asList("해고"),
                "answer_generation"
        ));
        권리의무.add(new FewShotExample(
                "ans-권리의무-007",
                "직원이 회사에 요구할 수 있는 권리는?",
                "직원이 회사에 요구할 수 있는 권리는 다음과 같습니다.\n\n취업규칙 제3조에 따르면:\n\n【임금 관련 권리】\n1. 정당한 임금 수령권\n   - 계약서에 명시된 임금 수령\n   - 각종 수당 및 상여금 수령\n\n2. 임금 명세서 수령권\n   - 매월 임금 명세서 제공 요구\n   - 세부 내역 확인 권리\n\n【근로 조건 관련 권리】\n1. 휴가 사용권\n   - 연차휴가, 경조휴가 사용\n   - 법정 휴일 휴무\n\n2. 근로 환경 보호권\n   - 안전하고 쾌적한 근로 환경\n   - 건강검진 등 복리후생\n\n3. 교육 기회 요구권\n   - 직무 교육 요구\n   - 승진 기회 요구",
                null,
                "취업규칙",
                "권리의무",
                Arrays.asList("취업규칙"),
                0.87,
                14,
                Arrays.asList("직원", "권리"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("권리의무", 권리의무);

        // 정보조회 예제 (5-7개)
        List<FewShotExample> 정보조회 = new ArrayList<>();
        정보조회.add(new FewShotExample(
                "ans-정보조회-001",
                "경조사에 대한 규정을 알려줘",
                "경조사 규정은 복리후생비규정과 취업규칙에 명시되어 있습니다.\n\n【경조휴가】\n복리후생비규정 제16조에 따르면:\n- 부모 사망: 5일\n- 배우자 사망: 5일\n- 자녀 사망: 3일\n- 형제자매 사망: 2일\n- 조부모 사망: 2일\n\n【경조금】\n복리후생비규정 제15조에 따르면:\n- 부모 사망: 50만원\n- 배우자 사망: 50만원\n- 자녀 사망: 30만원\n- 형제자매 사망: 20만원\n- 조부모 사망: 20만원\n\n경조휴가는 경조사 발생일로부터 30일 이내 사용해야 하며, 미사용 시 소멸됩니다.",
                null,
                "복리후생비규정",
                "정보조회",
                Arrays.asList("복리후생비규정", "취업규칙"),
                0.95,
                22,
                Arrays.asList("경조사"),
                "answer_generation"
        ));
        정보조회.add(new FewShotExample(
                "ans-정보조회-002",
                "출장 규정에 대해 알려줘",
                "출장 규정은 출장여비지급규정에 명시되어 있습니다.\n\n【출장비 구성】\n출장여비지급규정 제6조에 따르면:\n- 교통비: 실제 지출액 (영수증 기준)\n- 숙박비: 지역별 한도 내\n- 일비: 지역별 차등 지급\n\n【지역별 일비】\n- 수도권: 5만원\n- 광역시: 4만원\n- 기타 지역: 3만원\n\n【출장비 정산】\n- 출장 후 7일 이내 신청\n- 증빙서류 첨부 필수\n- 경리팀 확인 후 지급 (3~5일 소요)\n\n해외 출장의 경우 별도 기준이 적용됩니다.",
                null,
                "출장여비지급규정",
                "정보조회",
                Arrays.asList("출장여비지급규정"),
                0.93,
                19,
                Arrays.asList("출장"),
                "answer_generation"
        ));
        정보조회.add(new FewShotExample(
                "ans-정보조회-003",
                "법인카드 사용 규정을 알려줘",
                "법인카드 사용 규정은 경비지급규정에 명시되어 있습니다.\n\n【사용 용도】\n경비지급규정 제12조에 따르면:\n- 업무 관련 식대 및 접대비\n- 업무 관련 교통비\n- 업무 관련 구매비용\n\n【사용 한도】\n- 임원: 월 500만원\n- 부장: 월 300만원\n- 차장: 월 200만원\n- 과장: 월 100만원\n- 대리 이하: 월 50만원\n\n【사용 제한】\n- 개인 용도 사용 불가\n- 유흥업소 사용 불가\n- 한도 초과 사용 시 사전 승인 필요\n\n법인카드는 업무 용도로만 사용해야 하며, 위반 시 카드 회수 및 징계 대상이 될 수 있습니다.",
                null,
                "경비지급규정",
                "정보조회",
                Arrays.asList("경비지급규정"),
                0.91,
                17,
                Arrays.asList("법인카드"),
                "answer_generation"
        ));
        정보조회.add(new FewShotExample(
                "ans-정보조회-004",
                "건강검진에 대해 알려줘",
                "건강검진 규정은 복리후생비규정에 명시되어 있습니다.\n\n【검진 주기】\n복리후생비규정 제22조에 따르면:\n- 정기 건강검진: 매년 1회\n- 회사 부담\n- 전 직원 대상\n\n【검진 항목】\n- 기본 검진 항목\n- 직종별 추가 검진\n- 40세 이상: 암 검진 추가\n\n【검진 신청】\n- 인사팀에서 연간 검진 일정 공지\n- 지정 병원에서 예약 후 검진\n- 결과는 2주 후 온라인 확인 가능\n\n지정 병원 외 검진은 지원되지 않으며, 검진 기간 외 검진도 지원되지 않습니다.",
                null,
                "복리후생비규정",
                "정보조회",
                Arrays.asList("복리후생비규정"),
                0.89,
                15,
                Arrays.asList("건강검진"),
                "answer_generation"
        ));
        정보조회.add(new FewShotExample(
                "ans-정보조회-005",
                "교육비 지원 규정을 알려줘",
                "교육비 지원 규정은 복리후생비규정에 명시되어 있습니다.\n\n【지원 한도】\n복리후생비규정 제28조에 따르면:\n- 자격증 취득: 연간 200만원\n- 학자금 지원: 연간 100만원\n- 외국어 교육: 연간 50만원\n\n【지원 대상】\n- 업무 관련 교육\n- 사전 승인된 교육\n- 합격 또는 수료한 교육\n\n【지원 절차】\n1. 교육 사전 승인\n2. 교육 이수\n3. 교육비 정산 (영수증 및 수료증 제출)\n\n교육비 지원 후 1년 이내 퇴사 시 지원 금액의 50%를 반환해야 합니다.",
                null,
                "복리후생비규정",
                "정보조회",
                Arrays.asList("복리후생비규정"),
                0.87,
                13,
                Arrays.asList("교육비"),
                "answer_generation"
        ));
        정보조회.add(new FewShotExample(
                "ans-정보조회-006",
                "재택근무 규정을 알려줘",
                "재택근무 규정은 취업규칙에 명시되어 있습니다.\n\n【재택근무 조건】\n취업규칙 제25조에 따르면:\n- 업무 특성상 재택근무 가능한 업무\n- 부서장 사전 승인\n- 업무 연속성 확보 가능\n\n【재택근무 의무】\n- 정규 근무 시간 준수\n- 일일 업무 보고 필수\n- 정기적인 온라인 회의 참석\n\n【재택근무 제한】\n- 재택근무 중 출근 수당 지급 불가\n- 업무 집중도 평가 반영\n- 부서 업무 상황에 따라 제한 가능\n\n재택근무는 업무 효율성과 직원 복지를 고려하여 운영됩니다.",
                null,
                "취업규칙",
                "정보조회",
                Arrays.asList("취업규칙"),
                0.88,
                14,
                Arrays.asList("재택근무"),
                "answer_generation"
        ));
        정보조회.add(new FewShotExample(
                "ans-정보조회-007",
                "보안 규정에 대해 알려줘",
                "보안 규정은 보안관리규정에 명시되어 있습니다.\n\n【정보 보안】\n보안관리규정 제5조에 따르면:\n- 업무상 알게 된 기밀 정보 보호\n- 외부 유출 금지\n- 정보 보안 규정 준수\n\n【비밀 유지】\n- 재직 중뿐만 아니라 퇴사 후에도 유지\n- 계약 위반 시 손해배상 책임\n- 외부 유출 시 징계 대상\n\n【보안 조치】\n- 개인정보 보호\n- 시스템 접근 권한 관리\n- 정기적인 보안 교육\n\n보안 위반 시 중대한 징계를 받을 수 있으므로 각별히 주의해야 합니다.",
                null,
                "보안관리규정",
                "정보조회",
                Arrays.asList("보안관리규정"),
                0.86,
                12,
                Arrays.asList("보안"),
                "answer_generation"
        ));
        ANSWER_GENERATION_EXAMPLES.put("정보조회", 정보조회);
    }

    /**
     * 질문 분석용 Few-shot 예제 조회
     * @param limit 최대 개수 (기본 7개)
     * @return Few-shot 예제 목록
     */
    public static List<FewShotExample> getQueryAnalysisExamples(int limit) {
        return QUERY_ANALYSIS_EXAMPLES.stream()
                .sorted(Comparator.comparing(FewShotExample::getUsageFrequency).reversed()
                        .thenComparing(FewShotExample::getConfidenceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 질문 분석용 Few-shot 예제 조회 (기본 7개)
     */
    public static List<FewShotExample> getQueryAnalysisExamples() {
        return getQueryAnalysisExamples(7);
    }

    /**
     * 답변 생성용 Few-shot 예제 조회
     * @param intent 질문 의도
     * @param limit 최대 개수 (기본 7개)
     * @return Few-shot 예제 목록
     */
    public static List<FewShotExample> getAnswerGenerationExamples(String intent, int limit) {
        List<FewShotExample> examples = ANSWER_GENERATION_EXAMPLES.getOrDefault(intent, new ArrayList<>());
        return examples.stream()
                .sorted(Comparator.comparing(FewShotExample::getUsageFrequency).reversed()
                        .thenComparing(FewShotExample::getConfidenceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 답변 생성용 Few-shot 예제 조회 (기본 7개)
     */
    public static List<FewShotExample> getAnswerGenerationExamples(String intent) {
        return getAnswerGenerationExamples(intent, 7);
    }

    /**
     * Few-shot 예제를 프롬프트 형식으로 변환 (질문 분석용)
     */
    public static String formatQueryAnalysisExamples(List<FewShotExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n[Few-shot 예제]\n\n");

        for (int i = 0; i < examples.size(); i++) {
            FewShotExample example = examples.get(i);
            sb.append(String.format("예제 %d:\n", i + 1));
            sb.append(String.format("질문: \"%s\"\n", example.getQuestion()));
            sb.append(example.getAnalysisResult()).append("\n\n");
        }

        return sb.toString();
    }

    /**
     * Few-shot 예제를 프롬프트 형식으로 변환 (답변 생성용)
     */
    public static String formatAnswerGenerationExamples(List<FewShotExample> examples) {
        if (examples == null || examples.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n[Few-shot 예제]\n\n");

        for (int i = 0; i < examples.size(); i++) {
            FewShotExample example = examples.get(i);
            sb.append(String.format("예제 %d:\n", i + 1));
            sb.append(String.format("질문: %s\n", example.getQuestion()));
            sb.append(String.format("답변: %s\n\n", example.getAnswer()));
        }

        return sb.toString();
    }

    /**
     * 예제 사용 빈도 업데이트
     */
    public static void updateUsageFrequency(String exampleId) {
        // 질문 분석 예제 업데이트
        QUERY_ANALYSIS_EXAMPLES.stream()
                .filter(e -> e.getId().equals(exampleId))
                .forEach(e -> e.setUsageFrequency(e.getUsageFrequency() + 1));

        // 답변 생성 예제 업데이트
        ANSWER_GENERATION_EXAMPLES.values().stream()
                .flatMap(List::stream)
                .filter(e -> e.getId().equals(exampleId))
                .forEach(e -> e.setUsageFrequency(e.getUsageFrequency() + 1));
    }

    /**
     * 테스트용: 빈 예제 리스트 반환 (Few-shot 예제 비활성화)
     */
    public static List<FewShotExample> getEmptyQueryAnalysisExamples() {
        return Collections.emptyList();
    }

    /**
     * 테스트용: 빈 예제 리스트 반환 (Few-shot 예제 비활성화)
     */
    public static List<FewShotExample> getEmptyAnswerGenerationExamples(String intent) {
        return Collections.emptyList();
    }
}

