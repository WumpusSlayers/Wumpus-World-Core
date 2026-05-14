package com.wumpusslayers.wumpusworld.reasoning.service;

import com.wumpusslayers.wumpusworld.environment.domain.Percept;
import com.wumpusslayers.wumpusworld.environment.domain.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeUpdateServiceTest {

    private final KnowledgeUpdateService service = new KnowledgeUpdateService();

    private static Percept p(boolean stench, boolean breeze) {
        return Percept.builder()
                .stench(stench)
                .breeze(breeze)
                .glitter(false)
                .bump(false)
                .scream(false)
                .build();
    }

    @Test
    @DisplayName("(1,1)에서 breeze·stench 없음 관측이 KB에 저장된다.")
    void observeAtStartStoresSnapshot() {
        service.observe("u1", new Position(1, 1), p(false, false));

        var kb = service.getKnowledgeBaseOrNull("u1");
        assertNotNull(kb);
        assertTrue(kb.isVisited(new Position(1, 1)));
        assertFalse(kb.getCellBelief(new Position(1, 1)).lastPercept().isBreeze());
        assertFalse(kb.getCellBelief(new Position(1, 1)).lastPercept().isStench());
    }

    @Test
    @DisplayName("(1,1) 인접 위협을 가정한 percept 시퀀스가 그대로 누적된다.")
    void observeAdjacentStylePercepts() {
        service.observe("u1", new Position(1, 1), p(true, true));

        var kb = service.getKnowledgeBaseOrNull("u1");
        assertNotNull(kb);
        assertTrue(kb.getCellBelief(new Position(1, 1)).lastPercept().isStench());
        assertTrue(kb.getCellBelief(new Position(1, 1)).lastPercept().isBreeze());

        service.observe("u1", new Position(2, 1), p(false, true));
        assertTrue(kb.isVisited(new Position(2, 1)));
        assertTrue(kb.getCellBelief(new Position(2, 1)).lastPercept().isBreeze());
        assertFalse(kb.getCellBelief(new Position(2, 1)).lastPercept().isStench());
    }

    @Test
    @DisplayName("동일 세션·동일 칸·동일 percept 재호출은 멱등이다.")
    void repeatedObserveIsIdempotent() {
        Percept percept = p(false, true);
        service.observe("u1", new Position(1, 2), percept);
        service.observe("u1", new Position(1, 2), percept);

        var kb = service.getKnowledgeBaseOrNull("u1");
        assertNotNull(kb);
        assertEquals(percept, kb.getCellBelief(new Position(1, 2)).lastPercept());
    }

    @Test
    @DisplayName("세션별 KB가 분리된다.")
    void sessionsAreIsolated() {
        service.observe("a", new Position(1, 1), p(false, false));
        service.observe("b", new Position(2, 2), p(true, false));

        assertTrue(service.getKnowledgeBaseOrNull("a").isVisited(new Position(1, 1)));
        assertFalse(service.getKnowledgeBaseOrNull("a").isVisited(new Position(2, 2)));
        assertTrue(service.getKnowledgeBaseOrNull("b").isVisited(new Position(2, 2)));
    }

    @Test
    @DisplayName("resetSession 후 동일 sessionId로 observe하면 새 KB이다.")
    void resetSessionClearsKnowledge() {
        service.observe("u1", new Position(1, 1), p(false, false));
        service.resetSession("u1");
        assertNull(service.getKnowledgeBaseOrNull("u1"));

        service.observe("u1", new Position(1, 1), p(true, false));
        var kb = service.getKnowledgeBaseOrNull("u1");
        assertNotNull(kb);
        assertTrue(kb.getCellBelief(new Position(1, 1)).lastPercept().isStench());
    }

    @Test
    @DisplayName("Scream이 포함된 percept는 KB 전역 플래그에 반영된다.")
    void screamUpdatesGlobalFlags() {
        service.observe("u1", new Position(1, 2), Percept.builder()
                .stench(false).breeze(false).glitter(false).bump(false).scream(true)
                .build());
        assertFalse(service.getKnowledgeBaseOrNull("u1").isWumpusAlive());
        assertTrue(service.getKnowledgeBaseOrNull("u1").isHeardScream());
    }
}
