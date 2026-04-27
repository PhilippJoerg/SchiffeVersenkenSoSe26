package ui;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;

public class BoardDropListener extends TransferHandler {
    private final BoardPanel boardPanel;
    private final ShipPlacementController placementController;

    public BoardDropListener(BoardPanel boardPanel, ShipPlacementController placementController) {
        this.boardPanel = boardPanel;
        this.placementController = placementController;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(ShipDragData.FLAVOR);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            Transferable transferable = support.getTransferable();
            ShipDragData dragData = (ShipDragData) transferable.getTransferData(ShipDragData.FLAVOR);

            Point dropPoint = support.getDropLocation().getDropPoint();
            Point cell = boardPanel.cellAt(dropPoint);

            if (cell == null) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }

            boolean placed = placementController.placeShipFromDrag(
                    dragData.getShipType(),
                    cell.x,
                    cell.y,
                    dragData.getOrientation()
            );

            if (!placed) {
                Toolkit.getDefaultToolkit().beep();
            }

            return placed;
        } catch (UnsupportedFlavorException | IOException ex) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
    }
}