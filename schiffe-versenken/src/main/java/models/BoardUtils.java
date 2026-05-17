package models;

import java.util.Random;

public class BoardUtils {
    public static final int GRID_SIZE = 10;

    public static CellState[][] createEmptyCellBoard() {
        CellState[][] board = new CellState[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }
        return board;
    }

    public static boolean isInsideBoard(int col, int row) {
        return col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE;
    }

    public static boolean canPlaceShip(CellState[][] board, ShipType shipType, int startCol, int startRow,
            ShipOrientation orientation) {
        for (int i = 0; i < shipType.getSize(); i++) {
            int col = startCol;
            int row = startRow;
            if (orientation == ShipOrientation.HORIZONTAL) {
                col += i;
            } else {
                row += i;
            }
            if (!isInsideBoard(col, row) || board[col][row] != CellState.EMPTY || !isCellFree(board, col, row)) {
                return false;
            }
        }
        return true;
    }

    public static void placeShip(CellState[][] board, ShipType shipType, int startCol, int startRow,
            ShipOrientation orientation) {
        for (int i = 0; i < shipType.getSize(); i++) {
            int col = startCol;
            int row = startRow;
            if (orientation == ShipOrientation.HORIZONTAL) {
                col += i;
            } else {
                row += i;
            }
            board[col][row] = CellState.SHIP;
        }
    }

    public static void placeRandomShips(CellState[][] board) {
        Random random = new Random();
        for (ShipType shipType : ShipType.values()) {
            for (int i = 0; i < shipType.getAmount(); i++) {
                boolean placed = false;
                while (!placed) {
                    int col = random.nextInt(GRID_SIZE);
                    int row = random.nextInt(GRID_SIZE);
                    ShipOrientation orientation = random.nextBoolean() ? ShipOrientation.HORIZONTAL
                            : ShipOrientation.VERTICAL;
                    if (canPlaceShip(board, shipType, col, row, orientation)) {
                        placeShip(board, shipType, col, row, orientation);
                        placed = true;
                    }
                }
            }
        }
    }

    public static boolean isCellFree(CellState[][] board, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int checkX = x + i;
                int checkY = y + j;
                if (isInsideBoard(checkX, checkY)) {
                    if (board[checkX][checkY] == CellState.SHIP) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}