package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.junit.jupiter.api.Test;

import models.ShipOrientation;
import models.ShipType;

/**
 * de: Testet die Klasse ShipTransferable.
 * en: Tests the ShipTransferable class.
 */
class ShipTransferableTest {

    /**
     * de: Reagiert auf das Ereignis "TestSupportedFlavorAndDataRetrieval".
     * en: Reacts to the event "TestSupportedFlavorAndDataRetrieval".
     *
     * @throws Exception de: Bei Fehler waehrend der Ausfuehrung. en: If an error occurs during execution.
     */
    @Test
    void testSupportedFlavorAndDataRetrieval() throws Exception {
        ShipDragData data = new ShipDragData(ShipType.SUBMARINE, ShipOrientation.HORIZONTAL);
        ShipTransferable transferable = new ShipTransferable(data);

        DataFlavor flavor = ShipDragData.FLAVOR;
        assertTrue(transferable.isDataFlavorSupported(flavor));
        assertEquals(data, transferable.getTransferData(flavor));
    }

    /**
     * de: Reagiert auf das Ereignis "TestUnsupportedFlavorThrows".
     * en: Reacts to the event "TestUnsupportedFlavorThrows".
     *
     */
    @Test
    void testUnsupportedFlavorThrows() {
        ShipDragData data = new ShipDragData(ShipType.SUBMARINE, ShipOrientation.HORIZONTAL);
        ShipTransferable transferable = new ShipTransferable(data);
        assertThrows(UnsupportedFlavorException.class, () -> transferable.getTransferData(new DataFlavor(String.class, "String")));
    }
}
