package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import models.ShipOrientation;
import models.ShipType;

/**
 * de: Testet die Klasse ShipDragData.
 * en: Tests the ShipDragData class.
 */
class ShipDragDataTest {

    /**
     * de: Reagiert auf das Ereignis "TestGettersReturnValues".
     * en: Reacts to the event "TestGettersReturnValues".
     *
     */
    @Test
    void testGettersReturnValues() {
        ShipDragData data = new ShipDragData(ShipType.CRUISER, ShipOrientation.VERTICAL);
        assertSame(ShipType.CRUISER, data.getShipType());
        assertSame(ShipOrientation.VERTICAL, data.getOrientation());
    }

    /**
     * de: Reagiert auf das Ereignis "TestDataFlavorDescription".
     * en: Reacts to the event "TestDataFlavorDescription".
     *
     */
    @Test
    void testDataFlavorDescription() {
        assertEquals("ShipDragData", ShipDragData.FLAVOR.getHumanPresentableName());
    }
}
