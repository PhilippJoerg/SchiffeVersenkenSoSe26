package view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import models.CellState;

import org.junit.jupiter.api.Test;

class GameViewTest {

    @Test
    void testAnonymousImplementationCanReceiveUpdates() {
        GameView view = new GameView() {
            @Override
            public void setStatus(String text) {
            }

            @Override
            public void setOwnBoard(CellState[][] cells) {
            }

            @Override
            public void setEnemyBoard(CellState[][] cells) {
            }

            @Override
            public void setEnemyBoardClickListener(BoardClickListener listener) {
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
        };

        assertNotNull(view);
    }
}
