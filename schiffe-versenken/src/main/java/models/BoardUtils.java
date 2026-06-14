/*
 * Datei: models/BoardUtils.java
 * Hilfsfunktionen für das Spielfeld: Erzeugung, Platzierungsprüfungen und Zufallsplatzierung.
 */
package models;

import java.util.Map;
import java.util.Random;

public class BoardUtils {

    public static final int GRID_SIZE = 10;
    public static final int MAX_PLACEMENT_ATTEMPTS_PER_SHIP = 100;
    public static final int MAX_BOARD_RESTARTS = 100;

    /**
     * Erzeugt ein leeres 10x10-Spielfeld mit dem Zustand EMPTY.
     */
    public static CellState[][] createEmptyCellBoard() {
        return createEmptyCellBoard(GRID_SIZE);
    }

    public static CellState[][] createEmptyCellBoard(int boardSize) {
        CellState[][] board = new CellState[boardSize][boardSize];

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }

        return board;
    }

    /**
     * Setzt ein Board auf den Zustand EMPTY zurück.
     */
    public static void clearBoard(CellState[][] board) {
        int boardSize = board.length;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }
    }

    /**
     * Prüft, ob ein Feld innerhalb des gültigen Spielfeldbereichs liegt.
     */
    public static boolean isInsideBoard(CellState[][] board, int col, int row) {
        int boardSize = board.length;
        return col >= 0 && col < boardSize && row >= 0 && row < boardSize;
    }

    /**
     * Prüft, ob ein Schiff an der angegebenen Position und Ausrichtung
     * platziert werden kann.
     */
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
            if (!isInsideBoard(board, col, row) || board[col][row] != CellState.EMPTY || !isCellFree(board, col, row)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Platziert ein Schiff auf dem Board an der angegebenen Position.
     */
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

    public static void placeRandomShips(CellState[][] board, Map<ShipType, Integer> shipCounts) {
        Random random = new Random();
        int restartCount = 0;

        while (restartCount < MAX_BOARD_RESTARTS) {
            clearBoard(board);
            boolean success = true;

            for (ShipType shipType : ShipType.values()) {
                int amount = shipCounts.getOrDefault(shipType, 0);

                for (int i = 0; i < amount; i++) {
                    boolean placed = false;
                    int attemptCount = 0;

                    while (!placed && attemptCount < MAX_PLACEMENT_ATTEMPTS_PER_SHIP) {
                        int boardSize = board.length;
                        int col = random.nextInt(boardSize);
                        int row = random.nextInt(boardSize);
                        ShipOrientation orientation = random.nextBoolean()
                                ? ShipOrientation.HORIZONTAL
                                : ShipOrientation.VERTICAL;

                        if (canPlaceShip(board, shipType, col, row, orientation)) {
                            placeShip(board, shipType, col, row, orientation);
                            placed = true;
                        }

                        attemptCount++;
                    }

                    if (!placed) {
                        success = false;
                        break;
                    }
                }

                if (!success) {
                    break;
                }
            }

            if (success) {
                return;
            }

            restartCount++;
        }

        throw new IllegalStateException("Unable to place all ships after " + MAX_BOARD_RESTARTS + " restarts");
    }

    /**
     * Prüft, ob ein Feld und seine Nachbarfelder frei von Schiffen sind.
     */
    public static boolean isCellFree(CellState[][] board, int x, int y) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int checkX = x + i;
                int checkY = y + j;
                if (isInsideBoard(board, checkX, checkY)) {
                    if (board[checkX][checkY] == CellState.SHIP) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob das Schiff, zu dem die Zelle (col,row) gehört, bereits
     * vollständig versenkt ist. Akzeptiert Zellen mit Zustand HIT oder SHIP
     * (z.B. vor/ nach Auswertung).
     */
    public static boolean isShipSunkAt(CellState[][] board, int col, int row) {
        int boardSize = board.length;

        if (!isInsideBoard(board, col, row)) {
            return false;
        }
        CellState start = board[col][row];
        if (start != CellState.HIT && start != CellState.SHIP) {
            return false;
        }

        // helper to test ship segment
        java.util.function.Predicate<CellState> isSegment = s -> s == CellState.HIT || s == CellState.SHIP;

        // try horizontal
        int left = col;
        while (left - 1 >= 0 && isSegment.test(board[left - 1][row])) {
            left--;
        }
        int right = col;
        while (right + 1 < boardSize && isSegment.test(board[right + 1][row])) {
            right++;
        }
        if (right > left) {
            for (int c = left; c <= right; c++) {
                if (board[c][row] == CellState.SHIP) {
                    return false;
                }
            }
            return true;
        }

        // vertical
        int up = row;
        while (up - 1 >= 0 && isSegment.test(board[col][up - 1])) {
            up--;
        }
        int down = row;
        while (down + 1 < boardSize && isSegment.test(board[col][down + 1])) {
            down++;
        }
        for (int r = up; r <= down; r++) {
            if (board[col][r] == CellState.SHIP) {
                return false;
            }
        }
        return true;
    }
}
