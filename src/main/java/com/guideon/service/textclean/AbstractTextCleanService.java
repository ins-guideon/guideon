package com.guideon.service.textclean;

import java.text.Normalizer;

/**
 * 텍스트 전처리 서비스 추상 클래스
 * 공통 전처리 기능을 제공합니다.
 */
public abstract class AbstractTextCleanService implements TextCleanService {
    /**
     * [1단계] 기초 전처리 (가장 먼저 실행)
     * - OS 줄바꿈 통일, 유니코드 정규화, 제어문자 제거
     * - 이 작업은 노이즈 제거 전에 반드시 수행되어야 정규식이 잘 먹힙니다.
     */
    protected String cleanBasic(String text) {
        if (text == null || text.isEmpty()) return "";

        // 1. OS별 줄바꿈 문자 통일 (\r\n -> \n)
        text = text.replaceAll("\\r\\n|\\r", "\n");

        // 2. 유니코드 정규화 (NFC)
        text = Normalizer.normalize(text, Normalizer.Form.NFC);

        // 3. 눈에 보이지 않는 노이즈 및 제어 문자 제거
        text = text.replaceAll("[\uFEFF\u200B]", "");
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        return text;
    }

    /**
     * [2단계] 문장 병합 (순서 중요!)
     * - PDF 헤더를 지운 '다음에' 실행해야 데이터 손실이 없습니다.
     */
    protected String mergeBrokenLines(String text) {

        // 정규식 설명:
        // 1. ([^.\\n?!:;]) : 앞줄이 마침표, 물음표, 콜론 등으로 끝나지 않아야 함
        // 2. \\s*\\n\\s* : 줄바꿈 발생 (앞뒤 공백 무시)
        // 3. (?! ... )     : [부정형 전방 탐색] 뒷줄이 아래 패턴이면 병합 금지!

        // [병합 금지 패턴 목록]
        // - [-*•#>]             : 글머리 기호 (-, *, •, #, >)
        // - [0-9]+[.)]          : 숫자+점(1.) 또는 숫자+닫는괄호(1))
        // - [\\(\\[\\{][0-9]+[\\)\\]\\}] : 괄호로 감싸진 숫자 ((1), [1], {1})
        // - 제\\s*\\d+\\s*조      : 법조문 형식 (제 1 조, 제2조)
        // - 가\\. |나\\.         : 한글 목차 (가. 나. 다. - 필요시 추가)

        return text.replaceAll(
                "([^.\\n?!:;])\\s*\\n\\s*(?![-*•#>]|[0-9]+[.)]|[\\(\\[\\{][0-9]+[\\)\\]\\}]|제\\s*\\d+\\s*조|가\\.|나\\.|다\\.|라\\.|마\\.)",
                "$1 "
        );
    }

    /**
     * [3단계] 마무리 공백 정리
     */
    protected String cleanSpacing(String text) {
        // 과도한 줄바꿈 정리 (3개 이상 -> 2개)
        text = text.replaceAll("\\n{3,}", "\n\n");

        // 수평 공백 정규화 (연속된 스페이스 -> 1개)
        text = text.replaceAll("[ \\t]+", " ");

        return text.trim();
    }

    /**
     * (기본 구현) 일반 텍스트용 - 순차적 실행
     */
    protected String cleanCommon(String text) {
        text = cleanBasic(text);
        text = mergeBrokenLines(text); // 일반 파일은 바로 합쳐도 무방
        text = cleanSpacing(text);
        return text;
    }
}
