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

public class BoardDropListener extends TransferHandler {

    // Board, auf das ein Schiff gezogen wird
    private final BoardPanel boardPanel;

    // Controller für die Platzierungslogik
    private final ShipPlacementController placementController;

    public BoardDropListener(BoardPanel boardPanel, ShipPlacementController placementController) {
        this.boardPanel = boardPanel;
        this.placementController = placementController;
    }

    /*
     * Prüft, ob die aktuellen Drag-and-Drop-Daten angenommen werden können.
     * Erlaubt sind nur echte Drop-Aktionen mit ShipDragData.
     */
    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(ShipDragData.FLAVOR);
    }

    /*
     * Wird beim Ablegen eines Schiffs aufgerufen.
     * Liest die Drag-Daten aus, berechnet die Zielzelle und versucht,
     * das Schiff dort über den Controller zu platzieren.
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