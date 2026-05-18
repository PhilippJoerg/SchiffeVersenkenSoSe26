package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import models.ShipOrientation;
import models.ShipType;

class ShipDragDataTest {

    @Test
    void testGettersReturnValues() {
        ShipDragData data = new ShipDragData(ShipType.CRUISER, ShipOrientation.VERTICAL);
        assertSame(ShipType.CRUISER, data.getShipType());
        assertSame(ShipOrientation.VERTICAL, data.getOrientation());
    }

    @Test
    void testDataFlavorDescription() {
        assertEquals("ShipDragData", ShipDragData.FLAVOR.getHumanPresentableName());
    }
}
