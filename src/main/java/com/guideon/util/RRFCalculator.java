package com.guideon.util;

import com.guideon.model.ScoredSegment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reciprocal Rank Fusion (RRF) 계산기
 * 여러 검색 결과를 순위 기반으로 통합
 */
public class RRFCalculator {
    private static final int DEFAULT_K = 60;

    /**
     * Reciprocal Rank Fusion 점수 계산
     * score = 1 / (k + rank)
     *
     * @param rank 결과의 순위 (0부터 시작)
     * @param k RRF 상수 (기본값: 60)
     * @return RRF 점수
     */
    public static double calculateRRFScore(int rank, int k) {
        return 1.0 / (k + rank + 1);
    }

    /**
     * 가중치 적용 RRF 점수 계산
     *
     * @param rank 결과의 순위
     * @param k RRF 상수
     * @param weight 가중치 (0.0 ~ 1.0)
     * @return 가중치가 적용된 RRF 점수
     */
    public static double calculateWeightedRRFScore(int rank, int k, double weight) {
        return calculateRRFScore(rank, k) * weight;
    }

    /**
     * 여러 검색 결과 리스트를 RRF로 통합
     *
     * @param resultLists 검색 결과 리스트들 (각 리스트는 이미 순위대로 정렬됨)
     * @param weights 각 리스트의 가중치
     * @return 통합된 점수 맵 (segment ID -> 통합 점수)
     */
    public static Map<String, Double> mergeScores(
            List<List<ScoredSegment>> resultLists,
            List<Double> weights) {

        if (resultLists.size() != weights.size()) {
            throw new IllegalArgumentException("결과 리스트 수와 가중치 수가 일치하지 않습니다");
        }

        Map<String, Double> fusionScores = new HashMap<>();

        for (int listIdx = 0; listIdx < resultLists.size(); listIdx++) {
            List<ScoredSegment> results = resultLists.get(listIdx);
            double weight = weights.get(listIdx);

            for (int rank = 0; rank < results.size(); rank++) {
                ScoredSegment segment = results.get(rank);
                String id = segment.getId();
                double rrfScore = calculateWeightedRRFScore(rank, DEFAULT_K, weight);

                fusionScores.merge(id, rrfScore, Double::sum);
            }
        }

        return fusionScores;
    }

    /**
     * 두 검색 결과를 RRF로 통합 (간편 메서드)
     *
     * @param vectorResults Vector Search 결과
     * @param bm25Results BM25 Search 결과
     * @param vectorWeight Vector 가중치
     * @param bm25Weight BM25 가중치
     * @return 통합된 점수 맵
     */
    public static Map<String, Double> fuseTwoResults(
            List<ScoredSegment> vectorResults,
            List<ScoredSegment> bm25Results,
            double vectorWeight,
            double bm25Weight) {

        List<List<ScoredSegment>> resultLists = Arrays.asList(vectorResults, bm25Results);
        List<Double> weights = Arrays.asList(vectorWeight, bm25Weight);

        return mergeScores(resultLists, weights);
    }

    /**
     * 점수 맵을 정렬된 리스트로 변환
     *
     * @param fusionScores 점수 맵
     * @param segmentMap ID -> ScoredSegment 매핑
     * @param maxResults 최대 결과 수
     * @return 정렬된 ScoredSegment 리스트
     */
    public static List<ScoredSegment> sortAndLimitResults(
            Map<String, Double> fusionScores,
            Map<String, ScoredSegment> segmentMap,
            int maxResults) {

        return fusionScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> {
                    String id = entry.getKey();
                    double fusedScore = entry.getValue();
                    ScoredSegment original = segmentMap.get(id);

                    // 융합 점수로 새 ScoredSegment 생성
                    return new ScoredSegment(
                            id,
                            original.getSegment(),
                            fusedScore,
                            "HYBRID"
                    );
                })
                .collect(Collectors.toList());
    }
}
