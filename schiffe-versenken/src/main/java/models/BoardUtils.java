/*
 * Datei: models/BoardUtils.java
 * Hilfsfunktionen für das Spielfeld: Erzeugung, Platzierungsprüfungen und Zufallsplatzierung.
 */
package models;

import java.util.Random;

public class BoardUtils {
    public static final int GRID_SIZE = 10;

    /**
     * Erzeugt ein leeres 10x10-Spielfeld mit dem Zustand EMPTY.
     */
    public static CellState[][] createEmptyCellBoard() {
        CellState[][] board = new CellState[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                board[col][row] = CellState.EMPTY;
            }
        }
        return board;
    }

    /**
     * Prüft, ob ein Feld innerhalb des gültigen Spielfeldbereichs liegt.
     */
    public static boolean isInsideBoard(int col, int row) {
        return col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE;
    }

    /**
     * Prüft, ob ein Schiff an der angegebenen Position und Ausrichtung platziert werden kann.
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
            if (!isInsideBoard(col, row) || board[col][row] != CellState.EMPTY || !isCellFree(board, col, row)) {
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

    public static void placeRandomShips(CellState[][] board) {
        Random random = new Random();
        // Alle Schiffstypen (z. B. U-Boot, Kreuzer, ...) der Reihe nach durchgehen
        for (ShipType shipType : ShipType.values()) {
            // Jeden Typ so oft platzieren, wie es die Spielregeln vorgeben (z. B. 2× Zerstörer)
            for (int i = 0; i < shipType.getAmount(); i++) {
                boolean placed = false;
                // Solange würfeln, bis eine regelkonforme Position gefunden wurde
                while (!placed) {
                    int col = random.nextInt(GRID_SIZE);
                    int row = random.nextInt(GRID_SIZE);
                    // Zufällig horizontal oder vertikal ausrichten
                    ShipOrientation orientation = random.nextBoolean()
                            ? ShipOrientation.HORIZONTAL
                            : ShipOrientation.VERTICAL;
                    // Prüfen ob das Schiff an dieser Stelle passt (kein Rand-Überschreitung, kein Überlappen)
                    if (canPlaceShip(board, shipType, col, row, orientation)) {
                        placeShip(board, shipType, col, row, orientation);
                        placed = true; // Schleife beenden, nächstes Schiff platzieren
                    }
                }
            }
        }
    }

    /**
     * Prüft, ob ein Feld und seine Nachbarfelder frei von Schiffen sind.
     */
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