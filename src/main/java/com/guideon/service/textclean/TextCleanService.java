package com.guideon.service.textclean;

/**
 * 텍스트 전처리 서비스 인터페이스
 * 파일 형식에 따라 텍스트를 정제하고 정규화합니다.
 */
public interface TextCleanService {
    /**
     * 텍스트를 정제하고 정규화합니다.
     *
     * @param text 정제할 텍스트
     * @return 정제된 텍스트
     */
    String clean(String text);
}
