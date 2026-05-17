package view;

import models.CellState;

public interface GameView {
    void setStatus(String text);
    void setOwnBoard(CellState[][] cells);
    void setEnemyBoard(CellState[][] cells);
    void setEnemyBoardClickListener(BoardClickListener listener);
    void setShootButtonEnabled(boolean enabled);
    void setRotateButtonEnabled(boolean enabled);
}
