package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * de: Testet die Enum-Werte von CellState.
 * en: Tests the enum values of CellState.
 */
class CellStateTest {

    /**
     * de: Reagiert auf das Ereignis "TestEnumValues".
     * en: Responds to the "TestEnumValues" event.
     *
     */
    @Test
    void testEnumValues() {
        assertEquals(4, CellState.values().length);
        assertEquals(CellState.EMPTY, CellState.valueOf("EMPTY"));
        assertEquals(CellState.SHIP, CellState.valueOf("SHIP"));
        assertEquals(CellState.MISS, CellState.valueOf("MISS"));
        assertEquals(CellState.HIT, CellState.valueOf("HIT"));
    }
}
