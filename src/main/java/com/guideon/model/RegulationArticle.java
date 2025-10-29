package com.guideon.model;

/**
 * 규정 조항 정보
 * "제XX조", "제XX항" 등의 조항 번호와 내용을 표현
 */
public class RegulationArticle {
    private final String articleNumber;  // 예: "제32조", "제1항"
    private final String title;          // 예: "연차휴가"
    private final String content;        // 조항 내용
    private final String regulationType; // 규정 유형

    public RegulationArticle(String articleNumber, String title, String content, String regulationType) {
        this.articleNumber = articleNumber;
        this.title = title;
        this.content = content;
        this.regulationType = regulationType;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getRegulationType() {
        return regulationType;
    }

    @Override
    public String toString() {
        if (title != null && !title.isEmpty()) {
            return String.format("%s (%s)", articleNumber, title);
        }
        return articleNumber;
    }

    /**
     * 포맷팅된 조항 문자열 반환
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(articleNumber);
        if (title != null && !title.isEmpty()) {
            sb.append(" (").append(title).append(")");
        }
        if (content != null && !content.isEmpty()) {
            sb.append("\n").append(content);
        }
        return sb.toString();
    }
}
