package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.junit.jupiter.api.Test;

import models.ShipOrientation;
import models.ShipType;

class ShipTransferableTest {

    @Test
    void testSupportedFlavorAndDataRetrieval() throws Exception {
        ShipDragData data = new ShipDragData(ShipType.SUBMARINE, ShipOrientation.HORIZONTAL);
        ShipTransferable transferable = new ShipTransferable(data);

        DataFlavor flavor = ShipDragData.FLAVOR;
        assertTrue(transferable.isDataFlavorSupported(flavor));
        assertEquals(data, transferable.getTransferData(flavor));
    }

    @Test
    void testUnsupportedFlavorThrows() {
        ShipDragData data = new ShipDragData(ShipType.SUBMARINE, ShipOrientation.HORIZONTAL);
        ShipTransferable transferable = new ShipTransferable(data);
        assertThrows(UnsupportedFlavorException.class, () -> transferable.getTransferData(new DataFlavor(String.class, "String")));
    }
}
