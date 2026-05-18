/*
 * Datei: view/ShipTransferable.java
 * Transferable-Implementierung für Drag-and-Drop: liefert `ShipDragData` als Transfer-Daten.
 */
package view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public final class ShipTransferable implements Transferable {
    private final ShipDragData data;

    public ShipTransferable(ShipDragData data) {
        this.data = data;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ShipDragData.FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return ShipDragData.FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return data;
    }
}