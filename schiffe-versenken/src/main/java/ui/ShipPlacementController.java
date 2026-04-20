package ui;

import java.util.ArrayList;
import java.util.List;

public class ShipPlacementController {

    // Größe des Spielfelds: 10x10
    private static final int GRID_SIZE = 10;

    // Referenz auf das Hauptfenster für Status und Board-Updates
    private final MainFrame frame;
    // Eigenes Board für die Schiffplatzierung
    private final CellState[][] ownBoard;
    // Reihenfolge, in der die Schiffe gesetzt werden
    private final List<ShipType> placementOrder;

    // Index des aktuell zu platzierenden Schiffs
    private int currentShipIndex;
    // Aktuelle Ausrichtung des Schiffs
    private ShipOrientation currentOrientation;

    public ShipPlacementController(MainFrame frame) {
        this.frame = frame;
        this.ownBoard = createEmptyBoard();
        this.placementOrder = createPlacementOrder();
        this.currentShipIndex = 0;
        this.currentOrientation = ShipOrientation.HORIZONTAL;

        // Startzustand ins GUI laden
        frame.setOwnBoard(ownBoard);
        updateStatus();
        // Klicks auf das eigene Feld führen zur Platzierung
        frame.setOwnBoardClickListener(this::placeCurrentShip);
    }

    // Wechselt zwischen waagrecht und senkrecht
    public void rotateCurrentShip() {
        if (currentOrientation == ShipOrientation.HORIZONTAL) {
            currentOrientation = ShipOrientation.VERTICAL;
        } else {
            currentOrientation = ShipOrientation.HORIZONTAL;
        }

        updateStatus();
    }

    // Prüft, ob alle Schiffe bereits gesetzt wurden
    public boolean isPlacementFinished() {
        return currentShipIndex >= placementOrder.size();
    }

    // Versucht das aktuelle Schiff an der geklickten Position zu platzieren
    private void placeCurrentShip(int col, int row) {
        if (isPlacementFinished()) {
            frame.setStatus("Alle Schiffe sind bereits platziert.");
            return;
        }

        ShipType shipType = placementOrder.get(currentShipIndex);

        // Prüft, ob das Schiff dort gültig platziert werden kann
        if (!canPlaceShip(shipType, col, row, currentOrientation)) {
            frame.setStatus("Ungültige Position für " + shipType.getDisplayName() + ".");
            return;
        }

        // Trägt das Schiff ins Board ein
        applyShip(shipType, col, row, currentOrientation);
        currentShipIndex++;
        frame.setOwnBoard(ownBoard);

        if (isPlacementFinished()) {
            frame.setStatus("Alle Schiffe platziert.");
            return;
        }

        updateStatus();
    }

    // Prüft, ob das Schiff innerhalb des Boards liegt und keine Felder belegt sind
    private boolean canPlaceShip(ShipType shipType, int startCol, int startRow, ShipOrientation orientation) {
        for (int i = 0; i < shipType.getSize(); i++) {
            int col = startCol;
            int row = startRow;

            if (orientation == ShipOrientation.HORIZONTAL) {
                col += i;
            } else {
                row += i;
            }

            if (!isInsideBoard(col, row)) {
                return false;
            }

            if (ownBoard[row][col] != CellState.EMPTY) {
                return false;
            }
        }

        return true;
    }

    // Setzt alle Felder des Schiffs auf SHIP
    private void applyShip(ShipType shipType, int startCol, int startRow, ShipOrientation orientation) {
        for (int i = 0; i < shipType.getSize(); i++) {
            int col = startCol;
            int row = startRow;

            if (orientation == ShipOrientation.HORIZONTAL) {
                col += i;
            } else {
                row += i;
            }

            ownBoard[row][col] = CellState.SHIP;
        }
    }

    // Prüft, ob eine Position im gültigen 10x10-Bereich liegt
    private boolean isInsideBoard(int col, int row) {
        return col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE;
    }

    // Aktualisiert den Statustext mit aktuellem Schiff und Ausrichtung
    private void updateStatus() {
        if (isPlacementFinished()) {
            frame.setStatus("Alle Schiffe platziert.");
            return;
        }

        ShipType shipType = placementOrder.get(currentShipIndex);
        frame.setStatus(
                "Platziere: " + shipType.getDisplayName()
                        + " (" + shipType.getSize() + ")"
                        + " - Ausrichtung: " + orientationText()
        );
    }

    // Wandelt die aktuelle Ausrichtung in lesbaren Text um
    private String orientationText() {
        return currentOrientation == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }

    // Erstellt die Reihenfolge aller zu platzierenden Schiffe
    private List<ShipType> createPlacementOrder() {
        List<ShipType> result = new ArrayList<>();

        addShips(result, ShipType.BATTLESHIP);
        addShips(result, ShipType.CRUISER);
        addShips(result, ShipType.DESTROYER);
        addShips(result, ShipType.SUBMARINE);

        return result;
    }

    // Fügt einen Schiffstyp entsprechend seiner Anzahl mehrfach hinzu
    private void addShips(List<ShipType> list, ShipType type) {
        for (int i = 0; i < type.getAmount(); i++) {
            list.add(type);
        }
    }

    // Erstellt ein leeres Board nur mit EMPTY-Feldern
    private CellState[][] createEmptyBoard() {
        CellState[][] board = new CellState[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                board[row][col] = CellState.EMPTY;
            }
        }

        return board;
    }
}