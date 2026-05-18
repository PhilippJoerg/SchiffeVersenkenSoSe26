/*
 * Datei: models/ShipPlacementModel.java
 * Modell der Schiffplatzierung: hält das eigene Board, verbleibende Schiffe und die
 * aktuelle Ausrichtung. Stellt Methoden für Platzierung, Auto-Platzierung und Drehung bereit.
 */
package models;

import java.util.EnumMap;
import java.util.Map;

public class ShipPlacementModel {

    private final CellState[][] ownBoard;
    private final EnumMap<ShipType, Integer> remainingShips;
    private ShipOrientation currentOrientation;

    public ShipPlacementModel() {
        ownBoard = BoardUtils.createEmptyCellBoard();
        remainingShips = createRemainingShips();
        currentOrientation = ShipOrientation.HORIZONTAL;
    }

    /**
     * Liefert das eigene Spielfeld des Platzierungsmodells.
     */
    public CellState[][] getOwnBoard() {
        return ownBoard;
    }

    /**
     * Liefert die verbleibenden Schiffsmengen für jedes Schiffstyp.
     */
    public Map<ShipType, Integer> getRemainingShips() {
        return new EnumMap<>(remainingShips);
    }

    /**
     * Gibt die aktuell ausgewählte Schiffsausrichtung zurück.
     */
    public ShipOrientation getCurrentOrientation() {
        return currentOrientation;
    }

    /**
     * Dreht das aktuell zu platzierende Schiff zwischen horizontal und vertikal.
     */
    public void rotateCurrentShip() {
        currentOrientation = currentOrientation == ShipOrientation.HORIZONTAL
                ? ShipOrientation.VERTICAL
                : ShipOrientation.HORIZONTAL;
    }

    /**
     * Prüft, ob alle Schiffe bereits platziert wurden.
     */
    public boolean isPlacementFinished() {
        for (Integer remaining : remainingShips.values()) {
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Platziert ein Schiff über Drag-and-Drop an der angegebenen Position.
     */
    public boolean placeShipFromDrag(ShipType shipType, int col, int row, ShipOrientation orientation) {
        return placeShip(shipType, col, row, orientation);
    }

    /**
     * Platziert das nächste verfügbare Schiff an der angegebenen Position.
     */
    public boolean placeCurrentShip(int col, int row) {
        if (isPlacementFinished()) {
            return false;
        }

        ShipType shipType = getNextShipType();
        if (shipType == null) {
            return false;
        }

        return placeShip(shipType, col, row, currentOrientation);
    }

    /**
     * Platziert alle Schiffe zufällig und setzt die Ausrichtung zurück.
     */
    public void autoPlaceShips() {
        resetBoard();
        BoardUtils.placeRandomShips(ownBoard);
        for (ShipType shipType : ShipType.values()) {
            remainingShips.put(shipType, 0);
        }
        currentOrientation = ShipOrientation.HORIZONTAL;
    }

    /**
     * Platziert ein Schiff eines bestimmten Typs, wenn dies möglich ist.
     */
    public boolean placeShip(ShipType shipType, int col, int row, ShipOrientation orientation) {
        if (shipType == null) {
            return false;
        }

        if (getRemainingCount(shipType) <= 0) {
            return false;
        }

        if (!canPlaceShip(shipType, col, row, orientation)) {
            return false;
        }

        BoardUtils.placeShip(ownBoard, shipType, col, row, orientation);
        decrementRemaining(shipType);
        return true;
    }

    /**
     * Prüft, ob ein Schiff an der angegebenen Position platziert werden kann.
     */
    public boolean canPlaceShip(ShipType shipType, int startCol, int startRow, ShipOrientation orientation) {
        return BoardUtils.canPlaceShip(ownBoard, shipType, startCol, startRow, orientation);
    }

    /**
     * Liefert den nächsten Schiffstyp, der noch platziert werden muss.
     */
    public ShipType getNextShipType() {
        for (ShipType type : ShipType.values()) {
            if (getRemainingCount(type) > 0) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gibt die verbleibende Anzahl eines Schiffstyps zurück.
     */
    public int getRemainingCount(ShipType shipType) {
        return remainingShips.getOrDefault(shipType, 0);
    }

    /**
     * Verringert die verbleibende Anzahl eines Schiffstyps nach Platzierung.
     */
    private void decrementRemaining(ShipType shipType) {
        remainingShips.put(shipType, getRemainingCount(shipType) - 1);
    }

    /**
     * Setzt das eigene Board und die verbleibenden Schiffe auf den Ausgangszustand zurück.
     */
    private void resetBoard() {
        for (int col = 0; col < BoardUtils.GRID_SIZE; col++) {
            for (int row = 0; row < BoardUtils.GRID_SIZE; row++) {
                ownBoard[col][row] = CellState.EMPTY;
            }
        }
        for (ShipType type : ShipType.values()) {
            remainingShips.put(type, type.getAmount());
        }
    }

    /**
     * Erstellt die Map mit der Ausgangsanzahl jedes Schiffstyps.
     */
    private EnumMap<ShipType, Integer> createRemainingShips() {
        EnumMap<ShipType, Integer> result = new EnumMap<>(ShipType.class);
        for (ShipType type : ShipType.values()) {
            result.put(type, type.getAmount());
        }
        return result;
    }
} 
