package com.guideon.service.textclean;

import org.springframework.stereotype.Service;

/**
 * TXT 파일 전용 텍스트 전처리 서비스
 */
@Service
public class TxtTextCleanService extends AbstractTextCleanService {

    @Override
    public String clean(String text) {
        // 현재는 공통 처리만 수행 (필요시 확장 가능)
        return cleanCommon(text);
    }
}
