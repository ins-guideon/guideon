package com.guideon.service.textclean;

import org.springframework.stereotype.Service;

/**
 * 공통 텍스트 전처리 서비스
 * 확장자가 없거나 지원하지 않는 파일 형식에 사용됩니다.
 */
@Service
public class CommonTextCleanService extends AbstractTextCleanService {

    @Override
    public String clean(String text) {
        // 공통 전처리만 수행
        return cleanCommon(text);
    }
}
