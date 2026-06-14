package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import models.CellState;
import models.GameDifficulty;
import models.GameModel;
import models.BoardUtils;
import view.BoardClickListener;
import view.GameView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameControllerTest {

    private TestGameView view;
    private GameController gameController;
    private TestCom com;
    private GameModel model;

    static class TestGameView implements GameView {
        String status;
        CellState[][] ownBoard;
        CellState[][] enemyBoard;
        BoardClickListener listener;

        @Override
        public void setStatus(String text) {
            this.status = text;
        }

        @Override
        public void setOwnBoard(CellState[][] cells) {
            this.ownBoard = cells;
        }

        @Override
        public void setEnemyBoard(CellState[][] cells) {
            this.enemyBoard = cells;
        }

        @Override
        public void setEnemyBoardClickListener(BoardClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void setShootButtonEnabled(boolean enabled) {
        }

        @Override
        public void setRotateButtonEnabled(boolean enabled) {
        }

        @Override
        public void setAutoPlaceButtonEnabled(boolean enabled) {
        }

        @Override
        public void setSaveAction(Runnable action) {
        }

        @Override
        public void setLoadAction(Runnable action) {
        }
    }

    static class TestCom extends Com {
        private Listener currentListener;
        int lastAnswer = -1;
        boolean passSent;

        TestCom(Listener listener) {
            super(listener);
            this.currentListener = listener;
        }

        @Override
        public void sendAnswer(int answer) {
            lastAnswer = answer;
        }

        @Override
        public void sendPass() {
            passSent = true;
        }

        @Override
        public void setListener(Listener listener) {
            super.setListener(listener);
            this.currentListener = listener;
        }

        Listener getListener() {
            return currentListener;
        }
    }

    @BeforeEach
    void setUp() {
        view = new TestGameView();
        model = new GameModel(BoardUtils.createEmptyCellBoard(), GameDifficulty.EASY);
        com = new TestCom(null);
        gameController = new GameController(view, model, com, true);
    }

    @Test
    void testNetworkOnShotMissUpdatesStatusAndSendsAnswer() {
        model.getOwnBoard()[0][0] = CellState.EMPTY;
        Com.Listener listener = com.getListener();
        assertNotNull(listener);
        listener.onShot(0, 0);
        assertEquals(0, com.lastAnswer);
        assertTrue(view.status.contains("daneben") && view.status.contains("Warte auf pass"));
    }
}
