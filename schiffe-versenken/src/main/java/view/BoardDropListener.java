/*
 * Datei: view/BoardDropListener.java
 * TransferHandler für Drag-and-Drop von Schiffen auf das Board. Wandelt Drop-Koordinaten in
 * Spielfeldkoordinaten um und delegiert die Platzierung an `ShipPlacementController`.
 */
package view;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;

import controller.ShipPlacementController;

/**
 * de: BoardDropListener behandelt Drag-and-Drop-Ereignisse für das Platzieren von Schiffen auf dem Board.
 * en: BoardDropListener handles drag-and-drop events for placing ships on the board.
 */
public class BoardDropListener extends TransferHandler {

    // Board, auf das ein Schiff gezogen wird
    private final BoardPanel boardPanel;

    // Controller für die Platzierungslogik
    private final ShipPlacementController placementController;

    /**
     * de: Konstruktor für BoardDropListener.
     * en: Constructor for BoardDropListener.
     *
     * @param boardPanel de: Das Board, auf das Schiffe gezogen werden. en: The board to which ships are dragged.
     * @param placementController de: Controller für die Platzierungslogik. en: Controller for the placement logic.
     */
    public BoardDropListener(BoardPanel boardPanel, ShipPlacementController placementController) {
        this.boardPanel = boardPanel;
        this.placementController = placementController;
    }

    /*
     * de: Prüft, ob die aktuellen Drag-and-Drop-Daten angenommen werden können. Erlaubt sind nur echte Drop-Aktionen mit ShipDragData.
     * en: Checks if the current drag-and-drop data can be accepted. Only real drop actions with ShipDragData are allowed.
     */
    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(ShipDragData.FLAVOR);
    }

    /*
     * de: Wird beim Ablegen eines Schiffs aufgerufen. Liest die Drag-Daten aus, berechnet die Zielzelle und versucht, das Schiff dort über den Controller zu platzieren.
     * en: Called when a ship is dropped. Reads the drag data, calculates the target cell, and attempts to place the ship there via the controller.
     */
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            // Daten des gezogenen Schiffs auslesen
            Transferable transferable = support.getTransferable();
            ShipDragData dragData = (ShipDragData) transferable.getTransferData(ShipDragData.FLAVOR);

            // Drop-Position in Board-Koordinaten umrechnen
            Point dropPoint = support.getDropLocation().getDropPoint();
            Point cell = boardPanel.cellAt(dropPoint);

            // Drop außerhalb des Spielfelds ablehnen
            if (cell == null) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }

            // Schiff an der Zielposition platzieren
            boolean placed = placementController.placeShipFromDrag(
                    dragData.getShipType(),
                    cell.x,
                    cell.y,
                    dragData.getOrientation()
            );

            // Ungültige Platzierung akustisch melden
            if (!placed) {
                Toolkit.getDefaultToolkit().beep();
            }

            return placed;
        } catch (UnsupportedFlavorException | IOException ex) {
            // Fehlerhafte Drag-Daten ablehnen
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
    }
} 