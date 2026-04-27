package ui;

import java.util.EnumMap;
import java.util.Map;

public class ShipPlacementController {
    // Größe des Spielfelds: 10x10
    private static final int GRID_SIZE = 10;

    // Referenz auf das Hauptfenster für Status und Board-Updates
    private final MainFrame frame;

    // Eigenes Board für die Schiffplatzierung
    private final CellState[][] ownBoard;

    // Restanzahl je Schiffstyp
    private final EnumMap<ShipType, Integer> remainingShips;

    // Aktuelle Ausrichtung des Schiffs
    private ShipOrientation currentOrientation;

    public ShipPlacementController(MainFrame frame) {
        this.frame = frame;
        this.ownBoard = createEmptyBoard();
        this.remainingShips = createRemainingShips();
        this.currentOrientation = ShipOrientation.HORIZONTAL;

        // Startzustand ins GUI laden
        frame.setOwnBoard(ownBoard);
        frame.setShipPaletteRemainingCounts(remainingShips);
        frame.setShipPaletteOrientation(currentOrientation);

        // Klicks auf das eigene Feld führen weiterhin zur Platzierung des nächsten verfügbaren Schiffs
        frame.setOwnBoardClickListener(this::placeCurrentShip);

        // Drag-and-Drop auf das eigene Feld aktivieren
        frame.setOwnBoardTransferHandler(new BoardDropListener(frame.getOwnBoard(), this));

        updateStatus();
    }

    // Wechselt zwischen waagrecht und senkrecht
    public void rotateCurrentShip() {
        if (currentOrientation == ShipOrientation.HORIZONTAL) {
            currentOrientation = ShipOrientation.VERTICAL;
        } else {
            currentOrientation = ShipOrientation.HORIZONTAL;
        }

        frame.setShipPaletteOrientation(currentOrientation);
        updateStatus();
    }

    // Prüft, ob alle Schiffe bereits gesetzt wurden
    public boolean isPlacementFinished() {
        for (Integer remaining : remainingShips.values()) {
            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    // Wird vom Drag-and-Drop-Handler aufgerufen
    public boolean placeShipFromDrag(ShipType shipType, int col, int row, ShipOrientation orientation) {
        return placeShip(shipType, col, row, orientation);
    }

    // Versucht das nächste verfügbare Schiff an der geklickten Position zu platzieren
    private void placeCurrentShip(int col, int row) {
        if (isPlacementFinished()) {
            frame.setStatus("Alle Schiffe sind bereits platziert.");
            return;
        }

        ShipType shipType = getNextShipType();

        if (shipType == null) {
            frame.setStatus("Alle Schiffe sind bereits platziert.");
            return;
        }

        placeShip(shipType, col, row, currentOrientation);
    }

    // Zentrale Platzierungslogik für Klick und Drag-and-Drop
    private boolean placeShip(ShipType shipType, int col, int row, ShipOrientation orientation) {
        if (shipType == null) {
            return false;
        }

        if (getRemainingCount(shipType) <= 0) {
            frame.setStatus(shipType.getDisplayName() + " ist nicht mehr verfügbar.");
            return false;
        }

        // Prüft, ob das Schiff dort gültig platziert werden kann
        if (!canPlaceShip(shipType, col, row, orientation)) {
            frame.setStatus("Ungültige Position für " + shipType.getDisplayName() + ".");
            return false;
        }

        // Trägt das Schiff ins Board ein
        applyShip(shipType, col, row, orientation);
        decrementRemaining(shipType);

        frame.setOwnBoard(ownBoard);
        frame.setShipPaletteRemainingCounts(remainingShips);

        if (isPlacementFinished()) {
            frame.setStatus("Alle Schiffe platziert.");
            return true;
        }

        updateStatus();
        return true;
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

    // Aktualisiert den Statustext mit nächstem Schiff und Ausrichtung
    private void updateStatus() {
        if (isPlacementFinished()) {
            frame.setStatus("Alle Schiffe platziert.");
            return;
        }

        ShipType shipType = getNextShipType();

        frame.setStatus(
                "Platziere: " + shipType.getDisplayName() + " (" + shipType.getSize() + ")"
                        + " - Ausrichtung: " + orientationText()
        );
    }

    // Wandelt die aktuelle Ausrichtung in lesbaren Text um
    private String orientationText() {
        return currentOrientation == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }

    // Gibt den nächsten noch verfügbaren Schiffstyp zurück
    private ShipType getNextShipType() {
        for (ShipType type : ShipType.values()) {
            if (getRemainingCount(type) > 0) {
                return type;
            }
        }

        return null;
    }

    private int getRemainingCount(ShipType shipType) {
        return remainingShips.getOrDefault(shipType, 0);
    }

    private void decrementRemaining(ShipType shipType) {
        remainingShips.put(shipType, getRemainingCount(shipType) - 1);
    }

    // Erstellt die Restanzahl aller Schiffstypen aus dem Enum
    private EnumMap<ShipType, Integer> createRemainingShips() {
        EnumMap<ShipType, Integer> result = new EnumMap<>(ShipType.class);

        for (ShipType type : ShipType.values()) {
            result.put(type, type.getAmount());
        }

        return result;
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

    public Map<ShipType, Integer> getRemainingShips() {
        return new EnumMap<>(remainingShips);
    }
}