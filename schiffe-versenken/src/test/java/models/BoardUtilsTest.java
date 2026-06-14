package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoardUtilsTest {

    @Test
    void testCreateEmptyCellBoardHasEmptyCells() {
        CellState[][] board = BoardUtils.createEmptyCellBoard();
        assertEquals(BoardUtils.GRID_SIZE, board.length);
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            assertEquals(BoardUtils.GRID_SIZE, board[col].length);
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                assertEquals(CellState.EMPTY, board[col][row]);
            }
        }
    }

    @Test
    void testIsInsideBoardBoundsAndOutOfBounds() {
        assertTrue(BoardUtils.isInsideBoard(0, 0));
        assertTrue(BoardUtils.isInsideBoard(9, 9));
        assertFalse(BoardUtils.isInsideBoard(-1, 0));
        assertFalse(BoardUtils.isInsideBoard(0, -1));
        assertFalse(BoardUtils.isInsideBoard(BoardUtils.GRID_SIZE, 0));
        assertFalse(BoardUtils.isInsideBoard(0, BoardUtils.GRID_SIZE));
    }

    @Test
    void testCanPlaceShipValidAndInvalidPositions() {
        CellState[][] board = BoardUtils.createEmptyCellBoard();
        assertTrue(BoardUtils.canPlaceShip(board, ShipType.BATTLESHIP, 0, 0, ShipOrientation.HORIZONTAL));
        assertFalse(BoardUtils.canPlaceShip(board, ShipType.BATTLESHIP, 6, 0, ShipOrientation.HORIZONTAL));
        assertFalse(BoardUtils.canPlaceShip(board, ShipType.BATTLESHIP, 0, 6, ShipOrientation.VERTICAL));
        BoardUtils.placeShip(board, ShipType.BATTLESHIP, 0, 0, ShipOrientation.HORIZONTAL);
        assertFalse(BoardUtils.canPlaceShip(board, ShipType.CRUISER, 0, 0, ShipOrientation.HORIZONTAL));
    }

    @Test
    void testPlaceShipWritesShipCells() {
        CellState[][] board = BoardUtils.createEmptyCellBoard();
        BoardUtils.placeShip(board, ShipType.SUBMARINE, 2, 3, ShipOrientation.VERTICAL);
        assertEquals(CellState.SHIP, board[2][3]);
        assertEquals(CellState.SHIP, board[2][4]);
        assertEquals(CellState.EMPTY, board[2][5]);
    }

    @Test
    void testPlaceRandomShipsFillsShipsWithoutError() {
        CellState[][] board = BoardUtils.createEmptyCellBoard();
        BoardUtils.placeRandomShips(board);

        int shipCount = 0;
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                if (board[col][row] == CellState.SHIP) {
                    shipCount++;
                }
            }
        }

        int expectedShipCount = ShipType.BATTLESHIP.getAmount() * ShipType.BATTLESHIP.getSize()
                + ShipType.CRUISER.getAmount() * ShipType.CRUISER.getSize()
                + ShipType.DESTROYER.getAmount() * ShipType.DESTROYER.getSize()
                + ShipType.SUBMARINE.getAmount() * ShipType.SUBMARINE.getSize();

        assertEquals(expectedShipCount, shipCount);
    }

    @Test
    void testIsCellFreeOnEmptyAndNeighborCells() {
        CellState[][] board = BoardUtils.createEmptyCellBoard();
        assertTrue(BoardUtils.isCellFree(board, 0, 0));
        BoardUtils.placeShip(board, ShipType.SUBMARINE, 1, 1, ShipOrientation.HORIZONTAL);
        assertFalse(BoardUtils.isCellFree(board, 0, 0));
        assertFalse(BoardUtils.isCellFree(board, 1, 0));
        assertFalse(BoardUtils.isCellFree(board, 2, 0));
    }

    @Test
    void testPlaceShipWithInvalidBoardThrows() {
        CellState[][] badBoard = new CellState[9][9];
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> BoardUtils.placeShip(badBoard, ShipType.SUBMARINE, 8, 8, ShipOrientation.HORIZONTAL));
    }
}
