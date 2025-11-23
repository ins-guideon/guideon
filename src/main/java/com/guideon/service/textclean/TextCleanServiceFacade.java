package com.guideon.service.textclean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 텍스트 전처리 서비스 Facade
 * 파일 확장자에 따라 적절한 구현체를 선택하여 텍스트를 정제합니다.
 */
@Service
public class TextCleanServiceFacade {

    private static final Logger logger = LoggerFactory.getLogger(TextCleanServiceFacade.class);

    private final PdfTextCleanService pdfTextCleanService;
    private final WordTextCleanService wordTextCleanService;
    private final CommonTextCleanService commonTextCleanService;

    public TextCleanServiceFacade(
            PdfTextCleanService pdfTextCleanService,
            WordTextCleanService wordTextCleanService,
            CommonTextCleanService commonTextCleanService) {
        this.pdfTextCleanService = pdfTextCleanService;
        this.wordTextCleanService = wordTextCleanService;
        this.commonTextCleanService = commonTextCleanService;
    }

    /**
     * 파일 확장자에 따라 적절한 텍스트 정제 서비스를 선택하여 텍스트를 정제합니다.
     *
     * @param text          정제할 텍스트
     * @param fileExtension 파일 확장자 (예: "pdf", "docx", "txt")
     * @return 정제된 텍스트
     */
    public String clean(String text, String fileExtension) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (fileExtension == null || fileExtension.isEmpty()) {
            logger.debug("파일 확장자가 없어 공통 전처리만 수행합니다.");
            return commonTextCleanService.clean(text);
        }

        String extension = fileExtension.toLowerCase().replace(".", "");
        logger.debug("텍스트 다듬기 시작: 확장자={}, 원본 길이={}", extension, text.length());

        String cleaned;
        switch (extension) {
            case "pdf":
                cleaned = pdfTextCleanService.clean(text);
                break;
            case "doc":
            case "docx":
                cleaned = wordTextCleanService.clean(text);
                break;
            default:
                // txt 및 기타 확장자는 공통 전처리 사용
                logger.debug("확장자({})로 공통 전처리를 수행합니다.", extension);
                cleaned = commonTextCleanService.clean(text);
                break;
        }

        logger.debug("텍스트 다듬기 완료: 결과 길이={}", cleaned.length());
        return cleaned;
    }
}
