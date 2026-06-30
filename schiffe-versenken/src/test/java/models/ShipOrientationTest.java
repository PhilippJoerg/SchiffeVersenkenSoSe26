package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse ShipOrientation.
 * en: Tests the ShipOrientation class.
 */
class ShipOrientationTest {

    /**
     * de: Reagiert auf das Ereignis "TestEnumValues".
     * en: Responds to the "TestEnumValues" event.
     *
     */
    @Test
    void testEnumValues() {
        assertEquals(2, ShipOrientation.values().length);
        assertEquals(ShipOrientation.HORIZONTAL, ShipOrientation.valueOf("HORIZONTAL"));
        assertEquals(ShipOrientation.VERTICAL, ShipOrientation.valueOf("VERTICAL"));
    }
}
