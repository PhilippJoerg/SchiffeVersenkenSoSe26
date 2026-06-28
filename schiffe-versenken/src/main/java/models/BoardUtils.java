/*
 * Datei: models/BoardUtils.java
 * Hilfsfunktionen für das Spielfeld: Erzeugung, Platzierungsprüfungen und Zufallsplatzierung.
 */
package models;

import java.util.Map;
import java.util.Random;

/**
 * de: Die Klasse BoardUtils.
 * en: The BoardUtils class.
 */
public class BoardUtils {

    /**
     * de: Das Feld GRID_SIZE.
     * en: The field GRID_SIZE.
     */
    public static final int GRID_SIZE = 10;
    /**
     * de: Das Feld MAX_PLACEMENT_ATTEMPTS_PER_SHIP.
     * en: The field MAX_PLACEMENT_ATTEMPTS_PER_SHIP.
     */
    public static final int MAX_PLACEMENT_ATTEMPTS_PER_SHIP = 100;
    /**
     * de: Das Feld MAX_BOARD_RESTARTS.
     * en: The field MAX_BOARD_RESTARTS.
     */
    public static final int MAX_BOARD_RESTARTS = 100;

    /**
     * de: Erzeugt ein leeres 10x10-Spielfeld mit dem Zustand EMPTY.
     * en: Creates an empty 10x10 board with the state EMPTY.
     */
    public static CellState[][] createEmptyCellBoard() {
        return createEmptyCellBoard(GRID_SIZE);
    }

    /**
     * de: Erzeugt ein leeres Spielfeld mit der angegebenen Größe und dem Zustand EMPTY.
     * en: Creates an empty board with the specified size and the state EMPTY.
     *
     * @param boardSize de: Parameter boardSize. en: Parameter boardSize.
     * @return de: Rueckgabewert der Methode. en: Method return value.
     */
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
     * de: Setzt ein Board auf den Zustand EMPTY zurück.
     * en: Resets a board to the EMPTY state.
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
     * de: Prüft, ob ein Feld innerhalb des gültigen Spielfeldbereichs liegt.
     * en: Checks if a cell is within the valid board boundaries.
     */
    public static boolean isInsideBoard(CellState[][] board, int col, int row) {
        int boardSize = board.length;
        return col >= 0 && col < boardSize && row >= 0 && row < boardSize;
    }

    /**
     * de: Prueft, ob ein Schiff an der angegebenen Position mit der Ausrichtung platziert werden kann.
     * en: Checks whether a ship can be placed at the given position and orientation.
     *
     * @param board de: Spielfeld, das geprueft wird. en: Board to validate placement on.
     * @param shipType de: Zu platzierender Schiffstyp. en: Ship type to place.
     * @param startCol de: Startspalte des Schiffs. en: Start column of the ship.
     * @param startRow de: Startzeile des Schiffs. en: Start row of the ship.
     * @param orientation de: Ausrichtung des Schiffs. en: Orientation of the ship.
     * @return de: true bei gueltiger Platzierung, sonst false. en: true if placement is valid, otherwise false.
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
     * de: Platziert ein Schiff auf dem Spielfeld an der angegebenen Position.
     * en: Places a ship on the board at the given position.
     *
     * @param board de: Spielfeld, auf dem platziert wird. en: Board where the ship is placed.
     * @param shipType de: Zu platzierender Schiffstyp. en: Ship type to place.
     * @param startCol de: Startspalte des Schiffs. en: Start column of the ship.
     * @param startRow de: Startzeile des Schiffs. en: Start row of the ship.
     * @param orientation de: Ausrichtung des Schiffs. en: Orientation of the ship.
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

    /**
     * de: Platziert Schiffe zufällig auf dem Spielfeld basierend auf den angegebenen Schiffszahlen.
     * en: Places ships randomly on the board based on the specified ship counts.
     *
     * @param board de: Parameter board. en: Parameter board.
     * @param shipCounts de: Parameter shipCounts. en: Parameter shipCounts.
     */
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
     * de: Prüft, ob ein Feld und seine Nachbarfelder frei von Schiffen sind.
     * en: Checks if a cell and its neighboring cells are free of ships.
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
     * de: Prüft, ob das Schiff, zu dem die Zelle (col,row) gehört, bereits vollständig versenkt ist. Akzeptiert Zellen mit Zustand HIT oder SHIP (z.B. vor/ nach Auswertung).
     * en: Checks if the ship at the specified cell (col,row) is completely sunk. Accepts cells with state HIT or SHIP (e.g., before/after evaluation).
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
