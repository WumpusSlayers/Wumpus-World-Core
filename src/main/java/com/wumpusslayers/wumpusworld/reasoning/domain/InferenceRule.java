package com.wumpusslayers.wumpusworld.reasoning.domain;

/**
 * 전진 추론에 사용할 규칙 식별자. 적용 순서는 {@link #defaultPriority()} 오름차순을 권장하며,
 * 실제 루프·종료 조건은 {@link com.wumpusslayers.wumpusworld.reasoning.service.RuleEngineService}를 본다(#13·#25).
 */
public enum InferenceRule {

    /** 방문한 칸에 Breeze가 없으면, 인접한 미방문 칸에서 pit 가능성 제거 */
    NO_BREEZE_CLEAR_ADJACENT_PIT_CANDIDATES(10),

    /** 방문한 칸에 Stench가 없으면, 인접한 미방문 칸에서 wumpus 가능성 제거 */
    NO_STENCH_CLEAR_ADJACENT_WUMPUS_CANDIDATES(20),

    /** 방문한 칸에 Breeze가 있으면, 인접 미방문 칸들에 pit 후보 표시(#13) */
    BREEZE_MARK_PIT_CANDIDATES(30),

    /**
     * breeze 방문 칸이 2곳 이상일 때, 각 칸의 pit 설명 가능 인접 집합의 교집합 밖 pit 후보 제거(#25).
     * 교집합이 비면 적용하지 않는다(다중 pit 보수 처리).
     */
    BREEZE_PIT_INTERSECTION_NARROWS_CANDIDATES(32),

    /** 방문한 칸에 Stench가 있으면, 인접 미방문 칸들에 wumpus 후보 표시(#13) */
    STENCH_MARK_WUMPUS_CANDIDATES(40),

    /**
     * stench 방문 칸이 2곳 이상일 때, 각 칸의 wumpus 설명 가능 인접 집합의 교집합 밖 wumpus 후보 제거(#25).
     */
    STENCH_WUMPUS_INTERSECTION_NARROWS_CANDIDATES(42),

    /**
     * {@link KnowledgeBase#isWumpusAlive()} 가 false일 때 전 격자에서 움퍼스 후보를 제거한다.
     * 비명(percept)만으로 생존 플래그가 바뀌지 않으므로, 시뮬이 플래그를 맞춘 뒤 이 규칙이 동작한다(#25).
     */
    SCREAM_WUMPUS_ELIMINATED(50);

    private final int defaultPriority;

    /** 규칙별 기본 우선순위(낮을수록 먼저 적용하는 식으로 #13에서 사용 가능). */
    InferenceRule(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    /** 기본 우선순위 값을 반환한다. */
    public int defaultPriority() {
        return defaultPriority;
    }
}
