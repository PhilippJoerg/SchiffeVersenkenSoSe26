package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import models.CellState;
import models.GameDifficulty;
import models.GameSettings;
import models.GameModel;
import models.BoardUtils;
import view.BoardClickListener;
import view.GameView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse GameController.
 * en: Tests the GameController class.
 */
class GameControllerTest {

    private TestGameView view;
    private TestCom com;
    private GameModel model;

    static class TestGameView implements GameView {
        String status;
        CellState[][] ownBoard;
        CellState[][] enemyBoard;
        BoardClickListener listener;

        /**
         * de: Reagiert auf das Ereignis "Status".
         * en: Responds to the "Status" event.
         *
         * @param text de: Parameter text. en: Parameter text.
         */
        @Override
        public void setStatus(String text) {
            this.status = text;
        }

        /**
         * de: Reagiert auf das Ereignis "SetOwnBoard".
         * en: Responds to the "SetOwnBoard" event.
         *
         * @param cells de: Parameter cells. en: Parameter cells.
         */
        @Override
        public void setOwnBoard(CellState[][] cells) {
            this.ownBoard = cells;
        }

        /**
         * de: Reagiert auf das Ereignis "SetEnemyBoard".
         * en: Responds to the "SetEnemyBoard" event.
         *
         * @param cells de: Parameter cells. en: Parameter cells.
         */
        @Override
        public void setEnemyBoard(CellState[][] cells) {
            this.enemyBoard = cells;
        }

        /**
         * de: Reagiert auf das Ereignis "SetEnemyBoardClickListener".
         * en: Responds to the "SetEnemyBoardClickListener" event.
         *
         * @param listener de: Parameter listener. en: Parameter listener.
         */
        @Override
        public void setEnemyBoardClickListener(BoardClickListener listener) {
            this.listener = listener;
        }

        /**
         * de: Reagiert auf das Ereignis "SetShootButtonEnabled".
         * en: Responds to the "SetShootButtonEnabled" event.
         *
         * @param enabled de: Parameter enabled. en: Parameter enabled.
         */
        @Override
        public void setShootButtonEnabled(boolean enabled) {
        }

        /**
         * de: Reagiert auf das Ereignis "SetRotateButtonEnabled".
         * en: Responds to the "SetRotateButtonEnabled" event.
         *
         * @param enabled de: Parameter enabled. en: Parameter enabled.
         */
        @Override
        public void setRotateButtonEnabled(boolean enabled) {
        }

        /**
         * de: Reagiert auf das Ereignis "SetAutoPlaceButtonEnabled".
         * en: Responds to the "SetAutoPlaceButtonEnabled" event.
         *
         * @param enabled de: Parameter enabled. en: Parameter enabled.
         */
        @Override
        public void setAutoPlaceButtonEnabled(boolean enabled) {
        }

        /**
         * de: Reagiert auf das Ereignis "SetSaveAction".
         * en: Responds to the "SetSaveAction" event.
         *
         * @param action de: Parameter action. en: Parameter action.
         */
        @Override
        public void setSaveAction(Runnable action) {
        }

        /**
         * de: Reagiert auf das Ereignis "SetLoadAction".
         * en: Responds to the "SetLoadAction" event.
         *
         * @param action de: Parameter action. en: Parameter action.
         */
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

        /**
         * de: Reagiert auf das Ereignis "SendAnswer".
         * en: Responds to the "SendAnswer" event.
         *
         * @param answer de: Parameter answer. en: Parameter answer.
         */
        @Override
        public void sendAnswer(int answer) {
            lastAnswer = answer;
        }

        /**
         * de: Reagiert auf das Ereignis "SendPass".
         * en: Responds to the "SendPass" event.
         *
         */
        @Override
        public void sendPass() {
            passSent = true;
        }

        /**
         * de: Reagiert auf das Ereignis "SetListener".
         * en: Responds to the "SetListener" event.
         *
         * @param listener de: Parameter listener. en: Parameter listener.
         */
        @Override
        public void setListener(Listener listener) {
            super.setListener(listener);
            this.currentListener = listener;
        }

        /**
         * de: Reagiert auf das Ereignis "GetListener".
         * en: Responds to the "GetListener" event.
         *
         * @return de: Rueckgabewert der Methode. en: Method return value.
         */
        Listener getListener() {
            return currentListener;
        }
    }

    /**
     * de: Reagiert auf das Ereignis "SetUp".
     * en: Responds to the "SetUp" event.
     *
     */
    @BeforeEach
    void setUp() {
        view = new TestGameView();
        model = new GameModel(BoardUtils.createEmptyCellBoard(), GameDifficulty.EASY, GameSettings.defaultSettings());
        com = new TestCom(null);
        new GameController(view, model, com, true, () -> {
        });
    }

    /**
     * de: Reagiert auf das Ereignis "TestNetworkOnShotMissUpdatesStatusAndSendsAnswer".
     * en: Responds to the "TestNetworkOnShotMissUpdatesStatusAndSendsAnswer" event.
     *
     */
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
