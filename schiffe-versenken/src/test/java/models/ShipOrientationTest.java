package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ShipOrientationTest {

    @Test
    void testEnumValues() {
        assertEquals(2, ShipOrientation.values().length);
        assertEquals(ShipOrientation.HORIZONTAL, ShipOrientation.valueOf("HORIZONTAL"));
        assertEquals(ShipOrientation.VERTICAL, ShipOrientation.valueOf("VERTICAL"));
    }
}
