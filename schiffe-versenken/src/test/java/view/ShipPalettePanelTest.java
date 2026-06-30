package view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import models.ShipOrientation;
import models.ShipType;

/**
 * de: Testet die Klasse ShipPalettePanel.
 * en: Tests the ShipPalettePanel class.
 */
class ShipPalettePanelTest {

    /**
     * de: Reagiert auf das Ereignis "TestSetRemainingCountsAndOrientationDoesNotThrow".
     * en: Reacts to the event "TestSetRemainingCountsAndOrientationDoesNotThrow".
     *
     */
    @Test
    void testSetRemainingCountsAndOrientationDoesNotThrow() {
        ShipPalettePanel panel = new ShipPalettePanel();
        Map<ShipType, Integer> remaining = new EnumMap<>(ShipType.class);
        for (ShipType type : ShipType.values()) {
            remaining.put(type, 0);
        }

        assertDoesNotThrow(() -> panel.setRemainingCounts(remaining));
        assertDoesNotThrow(() -> panel.setOrientation(ShipOrientation.VERTICAL));
        assertNotNull(panel);
    }
}
