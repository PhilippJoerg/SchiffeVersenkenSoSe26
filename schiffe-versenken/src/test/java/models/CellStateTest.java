package models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CellStateTest {

    @Test
    void testEnumValues() {
        assertEquals(4, CellState.values().length);
        assertEquals(CellState.EMPTY, CellState.valueOf("EMPTY"));
        assertEquals(CellState.SHIP, CellState.valueOf("SHIP"));
        assertEquals(CellState.MISS, CellState.valueOf("MISS"));
        assertEquals(CellState.HIT, CellState.valueOf("HIT"));
    }
}
