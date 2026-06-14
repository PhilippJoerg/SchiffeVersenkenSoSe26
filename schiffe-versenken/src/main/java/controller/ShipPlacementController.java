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

public class ShipPlacementController {

    private final PlacementView frame;
    private final ShipPlacementModel model;
    private final Runnable onFinished;

    /**
     * Initialisiert den Platzierungs-Controller mit View und
     * Fertigstellungs-Callback.
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
    public void rotateCurrentShip() {
        model.rotateCurrentShip();
        frame.setShipPaletteRemainingCounts(model.getRemainingShips());
        frame.setShipPaletteOrientation(model.getCurrentOrientation());
        updateStatus();
    }

    /**
     * Platziert alle Schiffe automatisch zufällig auf dem eigenen Board.
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
     * Prüft, ob alle Schiffe bereits gesetzt wurden.
     */
    public boolean isPlacementFinished() {
        return model.isPlacementFinished();
    }

    /**
     * Versucht, ein Schiff per Drag-and-Drop auf dem Board zu platzieren.
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
     * Versucht das nächste verfügbare Schiff an der geklickten Position zu
     * platzieren.
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
     * Aktualisiert Board und Schiffspalette in der View nach einer Änderung.
     */
    private void refreshView() {
        frame.setOwnBoard(model.getOwnBoard());
        frame.setShipPaletteRemainingCounts(model.getRemainingShips());
        frame.setShipPaletteOrientation(model.getCurrentOrientation());
    }

    /**
     * Aktualisiert den Statustext mit dem nächsten zu platzierenden Schiff und
     * seiner Ausrichtung.
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
     * Wandelt die aktuelle Schiffsausrichtung in einen lesbaren Text um.
     */
    private String orientationText() {
        return model.getCurrentOrientation() == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }

    /**
     * Gibt die verbleibenden Schiffsmengen für die View zurück.
     */
    public Map<ShipType, Integer> getRemainingShips() {
        return model.getRemainingShips();
    }

    /**
     * Gibt das eigene Board für die Drag-and-Drop-Logik zurück.
     */
    public CellState[][] getOwnBoard() {
        return model.getOwnBoard();
    }
}
