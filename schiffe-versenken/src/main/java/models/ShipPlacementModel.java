/*
 * Datei: models/ShipPlacementModel.java
 * Modell der Schiffplatzierung: hält das eigene Board, verbleibende Schiffe und die
 * aktuelle Ausrichtung. Stellt Methoden für Platzierung, Auto-Platzierung und Drehung bereit.
 */
package models;

import java.util.EnumMap;
import java.util.Map;

/**
 * de: Die Klasse ShipPlacementModel verwaltet die Platzierung der Schiffe auf dem eigenen Spielfeld.
 * en: The class ShipPlacementModel manages the placement of ships on the player's own board.
 */
public class ShipPlacementModel {

    private final CellState[][] ownBoard;
    private final EnumMap<ShipType, Integer> remainingShips;
    private final GameSettings settings;
    private ShipOrientation currentOrientation;

    /**
     * de: Konstruktor für das ShipPlacementModel.
     * en: Constructor for the ShipPlacementModel.
     *
     * @param settings de: Parameter settings. en: Parameter settings.
     */
    public ShipPlacementModel(GameSettings settings) {
        this.settings = settings;
        ownBoard = BoardUtils.createEmptyCellBoard(settings.getBoardSize());
        remainingShips = createRemainingShips();
        currentOrientation = ShipOrientation.HORIZONTAL;
    }

    /**
     * de: Liefert das eigene Spielfeld des Platzierungsmodells.
     * en: Returns the player's own board from the placement model.
     */
    public CellState[][] getOwnBoard() {
        return ownBoard;
    }

    /**
     * de: Liefert die verbleibenden Schiffsmengen für jedes Schiffstyp.
     * en: Returns the remaining ship counts for each ship type.
     */
    public Map<ShipType, Integer> getRemainingShips() {
        return new EnumMap<>(remainingShips);
    }

    /**
     * de: Gibt die aktuell ausgewählte Schiffsausrichtung zurück.
     * en: Returns the currently selected ship orientation.
     */
    public ShipOrientation getCurrentOrientation() {
        return currentOrientation;
    }

    /**
     * de: Dreht das aktuell zu platzierende Schiff zwischen horizontal und vertikal.
     * en: Rotates the currently selected ship between horizontal and vertical.
     */
    public void rotateCurrentShip() {
        currentOrientation = currentOrientation == ShipOrientation.HORIZONTAL
                ? ShipOrientation.VERTICAL
                : ShipOrientation.HORIZONTAL;
    }

    /**
     * de: Prüft, ob alle Schiffe bereits platziert wurden.
     * en: Checks if all ships have been placed.

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
     * de: Platziert ein Schiff über Drag-and-Drop an der angegebenen Position.
     * en: Places a ship using drag-and-drop at the specified position.
     */
    public boolean placeShipFromDrag(ShipType shipType, int col, int row, ShipOrientation orientation) {
        return placeShip(shipType, col, row, orientation);
    }

    /**
     * de: Platziert das nächste verfügbare Schiff an der angegebenen Position.
     * en: Places the next available ship at the specified position.
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
     * de: Platziert alle Schiffe zufällig und setzt die Ausrichtung zurück.
     * en: Randomly places all ships and resets the orientation.
     */
    public void autoPlaceShips() {
        resetBoard();
        BoardUtils.placeRandomShips(ownBoard, settings.getShipCounts());
        for (ShipType shipType : ShipType.values()) {
            remainingShips.put(shipType, 0);
        }
        currentOrientation = ShipOrientation.HORIZONTAL;
    }

    /**
     * de: Platziert ein Schiff eines bestimmten Typs, wenn dies möglich ist.
     * en: Places a ship of a specific type if possible.
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
     * de: Prüft, ob ein Schiff an der angegebenen Position platziert werden kann.
     * en: Checks if a ship can be placed at the specified position.
     */
    public boolean canPlaceShip(ShipType shipType, int startCol, int startRow, ShipOrientation orientation) {
        return BoardUtils.canPlaceShip(ownBoard, shipType, startCol, startRow, orientation);
    }

    /**
     * de: Liefert den nächsten Schiffstyp, der noch platziert werden muss.
     * en: Returns the next ship type that still needs to be placed.
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
     * de: Gibt die verbleibende Anzahl eines Schiffstyps zurück.
     * en: Returns the remaining count of a specific ship type.
     */
    public int getRemainingCount(ShipType shipType) {
        return remainingShips.getOrDefault(shipType, 0);
    }

    /**
     * de: Verringert die verbleibende Anzahl eines Schiffstyps nach Platzierung.
     * en: Decrements the remaining count of a specific ship type after placement.
     */
    private void decrementRemaining(ShipType shipType) {
        remainingShips.put(shipType, getRemainingCount(shipType) - 1);
    }

    /**
     * de: Setzt das eigene Board und die verbleibenden Schiffe auf den Ausgangszustand zurück.
     * en: Resets the own board and the remaining ships to the initial state.
     */
    private void resetBoard() {
        BoardUtils.clearBoard(ownBoard);
        for (ShipType type : ShipType.values()) {
            remainingShips.put(type, settings.getShipCount(type));
        }
    }

    /**
     * de: Erstellt die Map mit der Ausgangsanzahl jedes Schiffstyps.
     * en: Creates the map with the initial count of each ship type.
     */
    private EnumMap<ShipType, Integer> createRemainingShips() {
        EnumMap<ShipType, Integer> result = new EnumMap<>(ShipType.class);
        for (ShipType type : ShipType.values()) {
            result.put(type, settings.getShipCount(type));
        }
        return result;
    }
}
