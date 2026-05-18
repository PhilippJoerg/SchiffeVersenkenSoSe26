package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import models.BoardUtils;
import models.CellState;

class BoardPanelTest {

    @Test
    void testCellAtInsideField() {
        BoardPanel panel = new BoardPanel(true);
        Point cell = panel.cellAt(new Point(BoardPanel.LABEL_SPACE + 1, BoardPanel.LABEL_SPACE + 1));
        assertEquals(new Point(0, 0), cell);
    }

    @Test
    void testCellAtOutsideFieldReturnsNull() {
        BoardPanel panel = new BoardPanel(true);
        assertEquals(null, panel.cellAt(new Point(0, 0)));
        assertEquals(null, panel.cellAt(new Point(BoardPanel.LABEL_SPACE + BoardPanel.GRID_SIZE * BoardPanel.CELL_SIZE + 10,
                BoardPanel.LABEL_SPACE + BoardPanel.GRID_SIZE * BoardPanel.CELL_SIZE + 10)));
    }

    @Test
    void testSetCellsWithInvalidBoardThrows() {
        BoardPanel panel = new BoardPanel(false);
        assertThrows(IllegalArgumentException.class, () -> panel.setCells(null));

        CellState[][] invalidBoard = new CellState[10][9];
        assertThrows(IllegalArgumentException.class, () -> panel.setCells(invalidBoard));
    }

    @Test
    void testSetCellsWithValidBoardSucceeds() {
        BoardPanel panel = new BoardPanel(false);
        CellState[][] board = BoardUtils.createEmptyCellBoard();
        panel.setCells(board);
        assertEquals(board[0][0], CellState.EMPTY);
    }
}
