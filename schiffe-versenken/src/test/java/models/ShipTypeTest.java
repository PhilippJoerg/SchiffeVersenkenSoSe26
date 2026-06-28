package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Klasse ShipType.
 * en: Tests the ShipType class.
 */
class ShipTypeTest {

    /**
     * de: Reagiert auf das Ereignis "TestGettersReturnCorrectValues".
     * en: Responds to the "TestGettersReturnCorrectValues" event.
     *
     */
    @Test
    void testGettersReturnCorrectValues() {
        assertEquals("Schlachtschiff", ShipType.BATTLESHIP.getDisplayName());
        assertEquals(1, ShipType.BATTLESHIP.getAmount());
        assertEquals(5, ShipType.BATTLESHIP.getSize());

        assertEquals("Kreuzer", ShipType.CRUISER.getDisplayName());
        assertEquals(2, ShipType.CRUISER.getAmount());
        assertEquals(4, ShipType.CRUISER.getSize());

        assertEquals("Zerstörer", ShipType.DESTROYER.getDisplayName());
        assertEquals(3, ShipType.DESTROYER.getAmount());
        assertEquals(3, ShipType.DESTROYER.getSize());

        assertEquals("U-Boot", ShipType.SUBMARINE.getDisplayName());
        assertEquals(4, ShipType.SUBMARINE.getAmount());
        assertEquals(2, ShipType.SUBMARINE.getSize());
    }

    /**
     * de: Reagiert auf das Ereignis "TestEnumValuesContainAllTypes".
     * en: Responds to the "TestEnumValuesContainAllTypes" event.
     *
     */
    @Test
    void testEnumValuesContainAllTypes() {
        assertEquals(4, ShipType.values().length);
    }
}
