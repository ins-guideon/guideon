package com.guideon.service.textclean;

import org.springframework.stereotype.Service;

/**
 * PDF 파일 전용 텍스트 전처리 서비스
 */
@Service
public class PdfTextCleanService extends AbstractTextCleanService {
    // 분석된 안전한 삭제 키워드 리스트
    private static final String SAFE_NOISE_KEYWORDS =
            "문서번호|제정일자|개정번호|개정일자|시행일자|" +
                    "Page|PAGE|" +
                    "INSPIEN \\(Confidential\\)|Confidential|" +
                    "The following table";

    @Override
    public String clean(String text) {
        if (text == null || text.isEmpty()) return "";

        // ========================================================
        // STEP 1. 기초 정제 (줄바꿈 통일 등)
        // ========================================================
        text = cleanBasic(text);

        // ========================================================
        // STEP 2. [핵심] 노이즈(헤더/푸터) 먼저 삭제!
        // ========================================================
        // 문장을 합치기 전에 쓰레기 줄을 먼저 날려야, 본문이 쓰레기와 합쳐져서
        // 같이 삭제되는 참사를 막을 수 있습니다.
        text = text.replaceAll("(?m)^.*(" + SAFE_NOISE_KEYWORDS + ").*$\\n?", "");

        // ========================================================
        // STEP 3. 문장 병합 (이제 안전함)
        // ========================================================
        // 헤더가 사라졌으므로, "12월" 뒤에 "31일"이 바로 붙게 됩니다.
        text = mergeBrokenLines(text);

        // PDF 전용 추가 병합 (한글-한글 사이 공백 없이 붙이기 등)이 필요하면 여기서 추가 수행
        // 예: text = text.replaceAll("([가-힣])\\s*\\n\\s*([가-힣])", "$1$2");

        // ========================================================
        // STEP 4. 마무리 (공백 정리)
        // ========================================================
        text = cleanSpacing(text);

        return text;
    }
}
