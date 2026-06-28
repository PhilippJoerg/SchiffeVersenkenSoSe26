package models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShipPlacementModelTest {

    private ShipPlacementModel model;

    @BeforeEach
    void setUp() {
        model = new ShipPlacementModel(GameSettings.defaultSettings());
    }

    @Test
    void testInitialStateHasAllShipsRemaining() {
        assertFalse(model.isPlacementFinished());
        assertEquals(1, model.getRemainingCount(ShipType.BATTLESHIP));
        assertEquals(2, model.getRemainingCount(ShipType.CRUISER));
        assertEquals(3, model.getRemainingCount(ShipType.DESTROYER));
        assertEquals(4, model.getRemainingCount(ShipType.SUBMARINE));
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
    }

    @Test
    void testRotateCurrentShipTogglesOrientation() {
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
        model.rotateCurrentShip();
        assertEquals(ShipOrientation.VERTICAL, model.getCurrentOrientation());
        model.rotateCurrentShip();
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
    }

    @Test
    void testPlaceCurrentShipAndRemainingCounts() {
        boolean result = model.placeCurrentShip(0, 0);
        assertTrue(result);
        assertEquals(0, model.getRemainingCount(ShipType.BATTLESHIP));
        assertEquals(2, model.getRemainingCount(ShipType.CRUISER));
    }

    @Test
    void testPlaceShipFromDragWithNullReturnsFalse() {
        assertFalse(model.placeShipFromDrag(null, 0, 0, ShipOrientation.HORIZONTAL));
    }

    @Test
    void testAutoPlaceShipsFinishesPlacement() {
        model.autoPlaceShips();
        assertTrue(model.isPlacementFinished());
        assertEquals(ShipOrientation.HORIZONTAL, model.getCurrentOrientation());
    }

    @Test
    void testGetNextShipTypeReturnsNullAfterAllPlaced() {
        model.autoPlaceShips();
        assertTrue(model.isPlacementFinished());
        assertEquals(null, model.getNextShipType());
    }
}
