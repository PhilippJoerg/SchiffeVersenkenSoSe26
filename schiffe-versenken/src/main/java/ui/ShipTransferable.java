<<<<<<< HEAD:schiffe-versenken/src/main/java/ui/ShipTransferable.java
package ui;
=======
<<<<<<< HEAD:schiffe-versenken/src/main/java/view/ShipTransferable.java
package view;
=======
package ui;
>>>>>>> 4f43638 (added drag n drop for ships):schiffe-versenken/src/main/java/ui/ShipTransferable.java
>>>>>>> 44456cf (added drag n drop for ships):schiffe-versenken/src/main/java/view/ShipTransferable.java

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