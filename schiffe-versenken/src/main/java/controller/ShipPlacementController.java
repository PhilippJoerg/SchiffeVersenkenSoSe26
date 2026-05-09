package controller;

import java.util.Map;

import models.ShipOrientation;
import models.ShipPlacementModel;
import models.ShipType;
import view.BoardDropListener;
import view.CellState;
import view.MainFrame;

public class ShipPlacementController {
    private final MainFrame frame;
    private final ShipPlacementModel model;
    private final Runnable onFinished;

    public ShipPlacementController(MainFrame frame, Runnable onFinished) {
        this.frame = frame;
        this.model = new ShipPlacementModel();
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

    // Prüft, ob alle Schiffe bereits gesetzt wurden
    public boolean isPlacementFinished() {
        return model.isPlacementFinished();
    }

    // Wird vom Drag-and-Drop-Handler aufgerufen
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

    // Versucht das nächste verfügbare Schiff an der geklickten Position zu platzieren
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

    private void refreshView() {
        frame.setOwnBoard(model.getOwnBoard());
        frame.setShipPaletteRemainingCounts(model.getRemainingShips());
    }

    // Aktualisiert den Statustext mit nächstem Schiff und Ausrichtung
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

    // Wandelt die aktuelle Ausrichtung in lesbaren Text um
    private String orientationText() {
        return model.getCurrentOrientation() == ShipOrientation.HORIZONTAL ? "waagrecht" : "senkrecht";
    }

    public Map<ShipType, Integer> getRemainingShips() {
        return model.getRemainingShips();
    }

    public CellState[][] getOwnBoard() {
        return model.getOwnBoard();
    }
}