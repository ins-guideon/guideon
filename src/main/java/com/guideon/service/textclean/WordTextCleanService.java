package com.guideon.service.textclean;

import org.springframework.stereotype.Service;

/**
 * Word 파일(.docx, .doc) 전용 텍스트 전처리 서비스
 */
@Service
public class WordTextCleanService extends AbstractTextCleanService {

    @Override
    public String clean(String text) {
        if (text == null || text.isEmpty()) return "";

        // ========================================================
        // STEP 1. 기초 정제 (OS 줄바꿈 통일, 유니코드 정규화)
        // ========================================================
        // Word 파일도 맥/윈도우 작성 환경에 따라 줄바꿈이 다를 수 있으므로 필수입니다.
        text = cleanBasic(text);

        // ========================================================
        // STEP 2. [Word 전용] 스마트 따옴표 및 특수 기호 변환
        // ========================================================
        // "기울어진 따옴표"나 "긴 대시"를 표준 문자로 통일합니다.
        text = text.replaceAll("[\u201C\u201D]", "\""); // “ ” -> "
        text = text.replaceAll("[\u2018\u2019]", "'");  // ‘ ’ -> '
        text = text.replaceAll("[\u2013\u2014]", "-");  // – — -> - (긴 대시)

        // ========================================================
        // STEP 3. 마무리 공백 정리
        // ========================================================
        // 주의: Word는 줄바꿈이 '문단 구분'이므로 Abstract에 있는 'mergeBrokenLines'(문장 병합)는
        // 호출하지 않는 것이 원칙입니다. (호출하면 제목과 본문이 붙어버림)

        // cleanSpacing 내부에서 '\n{3,}' -> '\n\n' 처리와 다중 공백 제거를 수행합니다.
        text = cleanSpacing(text);

        return text;
    }
}
