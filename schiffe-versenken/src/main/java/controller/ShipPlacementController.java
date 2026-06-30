package controller;

/*
 * Datei: controller/ShipPlacementController.java
 * Verwaltet die Logik zur Platzierung von Schiffen: Drehung, automatische Platzierung,
 * Platzierung per Klick/Drag-and-Drop und Statusaktualisierungen für die View.
 */
import java.util.Map;

import models.CellState;
import models.GameSettings;
import models.ShipOrientation;
import models.ShipPlacementModel;
import models.ShipType;
import view.BoardDropListener;
import view.PlacementView;

/**
 * de: Verwaltet die Logik zur Platzierung von Schiffen.
 * en: Manages the logic for ship placement.
 */
public class ShipPlacementController {

    private final PlacementView frame;
    private final ShipPlacementModel model;
    private final Runnable onFinished;

    /**
     * de: Initialisiert den Platzierungs-Controller mit View und Fertigstellungs-Callback.
     * en: Initializes the ship placement controller with view and completion callback.
     */
    public ShipPlacementController(PlacementView frame, Runnable onFinished, GameSettings settings) {
        this.frame = frame;
        this.model = new ShipPlacementModel(settings);
        this.onFinished = onFinished;

        frame.setOwnBoard(model.getOwnBoard());
        frame.setShipPaletteRemainingCounts(model.getRemainingShips());
        frame.setShipPaletteOrientation(model.getCurrentOrientation());

        frame.setOwnBoardClickListener(this::placeCurrentShip);
        frame.setOwnBoardTransferHandler(new BoardDropListener(frame.getOwnBoard(), this));

        updateStatus();
    }

    // Wechselt zwischen waagrecht und senkrecht
    /**
     * de: Dreht das aktuell ausgewählte Schiff.
     * en: Rotates the currently selected ship.
     *
     */
    public void rotateCurrentShip() {
        model.rotateCurrentShip();
        frame.setShipPaletteRemainingCounts(model.getRemainingShips());
        frame.setShipPaletteOrientation(model.getCurrentOrientation());
        updateStatus();
    }

    /**
     * de: Platziert alle Schiffe automatisch zufällig auf dem eigenen Board.
     * en: Automatically places all ships randomly on the player's board.
     */
    public void autoPlaceShips() {
        if (model.isPlacementFinished()) {
            frame.setStatus("Alle Schiffe sind bereits platziert.");
            return;
        }
        model.autoPlaceShips();
        refreshView();
        frame.setStatus("Schiffe automatisch platziert.");
        if (model.isPlacementFinished()) {
            onFinished.run();
        }
    }

    /**
     * de: Prüft, ob alle Schiffe bereits gesetzt wurden.
     * en: Checks if all ships have been placed.
     */
    public boolean isPlacementFinished() {
        return model.isPlacementFinished();
    }

    /**
     * de: Versucht, ein Schiff per Drag-and-Drop auf dem Board zu platzieren.
     * en: Attempts to place a ship on the board via drag-and-drop.
     */
    public boolean placeShipFromDrag(ShipType shipType, int col, int row, ShipOrientation orientation) {
        boolean placed = model.placeShipFromDrag(shipType, col, row, orientation);
        if (placed) {
            refreshView();
            if (model.isPlacementFinished()) {
                frame.setStatus("Alle Schiffe platziert.");
                onFinished.run();
            }
        }
        return placed;
    }

    /**
     * de: Versucht das nächste verfügbare Schiff an der geklickten Position zu platzieren.
     * en: Attempts to place the next available ship at the clicked position.
     */
    private void placeCurrentShip(int col, int row) {
        if (model.isPlacementFinished()) {
            frame.setStatus("Alle Schiffe sind bereits platziert.");
            return;
        }

        ShipType shipType = model.getNextShipType();
        if (shipType == null) {
            frame.setStatus("Alle Schiffe sind bereits platziert.");
            return;
        }

        boolean placed = model.placeCurrentShip(col, row);
        if (!placed) {
            if (model.getRemainingCount(shipType) <= 0) {
                frame.setStatus(shipType.getDisplayName() + " ist nicht mehr verfügbar.");
            } else if (!model.canPlaceShip(shipType, col, row, model.getCurrentOrientation())) {
                frame.setStatus("Ungültige Position für " + shipType.getDisplayName() + ".");
            } else {
                frame.setStatus("Konnte das Schiff nicht platzieren.");
            }
            return;
        }

        refreshView();
        if (model.isPlacementFinished()) {
            frame.setStatus("Alle Schiffe platziert.");
            onFinished.run();
        } else {
            updateStatus();
        }
    }

    /**
     * de: Aktualisiert Board und Schiffspalette in der View nach einer Änderung.
     * en: Updates the board and ship palette in the view after a change.
     */
    private void refreshView() {
        frame.setOwnBoard(model.getOwnBoard());
        frame.setShipPaletteRemainingCounts(model.getRemainingShips());
        frame.setShipPaletteOrientation(model.getCurrentOrientation());
    }

    /**
     * de: Aktualisiert den Statustext mit dem nächsten zu platzierenden Schiff und seiner Ausrichtung.
     * en: Updates the status text with the next ship to be placed and its orientation.
     */
    private void updateStatus() {
        if (model.isPlacementFinished()) {
            frame.setStatus("Alle Schiffe platziert.");
            return;
        }

        ShipType shipType = model.getNextShipType();
        frame.setStatus(
                "Platziere: " + shipType.getDisplayName() + " (" + shipType.getSize() + ")"
                + " - Ausrichtung: " + orientationText());
    }

    /**
     * de: Wandelt die aktuelle Schiffsausrichtung in einen lesbaren Text um.
     * en: Converts the current ship orientation to a readable text.
     */
    private String orientationText() {
        return model.getCurrentOrientation() == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }

    /**
     * de: Gibt die verbleibenden Schiffsmengen für die View zurück.
     * en: Returns the remaining ship counts for the view.
     */
    public Map<ShipType, Integer> getRemainingShips() {
        return model.getRemainingShips();
    }

    /**
     * de: Gibt das eigene Board für die Drag-and-Drop-Logik zurück.
     * en: Returns the player's own board for drag-and-drop logic.
     */
    public CellState[][] getOwnBoard() {
        return model.getOwnBoard();
    }
}
